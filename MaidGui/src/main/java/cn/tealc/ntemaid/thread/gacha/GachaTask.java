package cn.tealc.ntemaid.thread.gacha;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Wincon;
import com.sun.jna.win32.StdCallLibrary;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 抽卡数据导出任务：调用 gacha/nte-gacha-exporter-cli.exe 控制台程序，
 * 通过 stdout 检测 "json=<path>" 作为完成信号，并支持通过 stdin 发送 "q" 手动停止。
 * <p>
 * 组件由 {@link GachaExporterManager} 管理：启动前自动检测版本，
 * 过旧时（无法适配当前游戏卡池）自动从上游下载更新。
 */
public class GachaTask extends Task<File> {
    private static final Logger log = LoggerFactory.getLogger(GachaTask.class);

    /**
     * Kernel32 扩展：JNA 未映射的 SetConsoleCtrlHandler
     * （传 null handler + add=true 可忽略本进程收到的 Ctrl+C）。
     */
    private interface Kernel32Ext extends StdCallLibrary {
        Kernel32Ext INSTANCE = Native.load("kernel32", Kernel32Ext.class);

        boolean SetConsoleCtrlHandler(Pointer handler, boolean add);
    }

    /** 优雅停止的最长等待时间（秒），超时后强制结束进程 */
    private static final int STOP_GRACE_SECONDS = 15;

    private final boolean autoPage;
    private Process process;
    private BufferedWriter writer; // 向进程 stdin 写入指令
    private final AtomicBoolean completed = new AtomicBoolean(false);

    public GachaTask(boolean autoPage) {
        this.autoPage = autoPage;
    }

    @Override
    protected File call() {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("ntemaid-gacha-", ".json");

            // 确保抓包组件存在且版本满足要求（过旧时自动下载更新，适配新版本游戏卡池）
            GachaExporterManager.ensureExporter(this::updateMessage);

            // 构建命令行
            String exePath = GachaExporterManager.getExporterFile().getAbsolutePath();
            List<String> command = new ArrayList<>();
            command.add(exePath);
            command.add("capture");
            command.add("--json");
            command.add(tempFile.toAbsolutePath().toString());
            command.add("--locale");
            command.add("zh-CN");
            if (autoPage) {
                command.add("--auto-page");
            }

            // 启动进程
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            process = pb.start();

            // 获取 stdin writer，用于发送 'q' 停止
            writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));

            // 读取 stdout，检测 "json=<tempFile绝对路径>" 作为完成标记
            String completionMarker = "json=" + tempFile.toAbsolutePath().toString();
            log.debug("临时文件：{}", completionMarker);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug(line);
                    if (line.contains(completionMarker)) {
                        log.debug("抓取结束");
                        completed.set(true);
                        reapProcessAsync();
                        return tempFile.toFile();
                    }
                    updateMessage(line);
                }
            }

            // 确保进程退出：等待最多 30 秒
            if (process.isAlive()) {
                if (!process.waitFor(30, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    log.warn("Gacha 进程超时未退出，已强制终止");
                }
            }

            if (completed.get()) {
                log.warn("Gacha 进程结束，发送文件");
                return tempFile.toFile();
            } else {
                log.warn("Gacha 进程结束但未检测到完成标记");
                return null;
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 手动停止抓取。
     * <p>
     * 由于 Java 以管道方式承载子进程 stdin，exporter 不会监听管道中的 'q'，
     * 因此这里依次尝试：发送 'q'（兼容未来版本）→ 向子进程控制台发送
     * Ctrl+C 事件（exporter 会优雅收尾并输出结果文件）→ 等待优雅退出，
     * 超时仍未退出则强制终止，避免进程残留。
     */
    public void stop() {
        Process target = this.process;
        if (target == null || !target.isAlive()) {
            return;
        }

        // 1. 尝试旧协议：stdin 发送 q
        try {
            if (writer != null) {
                writer.write("q\n");
                writer.flush();
            }
        } catch (IOException e) {
            log.debug("发送停止指令失败: {}", e.getMessage());
        }

        // 2/3. 在后台线程发送 Ctrl+C 并等待退出，避免阻塞 UI 线程
        Thread.startVirtualThread(() -> {
            try {
                sendCtrlC(target);
                long deadline = System.currentTimeMillis() + STOP_GRACE_SECONDS * 1000L;
                while (target.isAlive() && System.currentTimeMillis() < deadline) {
                    if (completed.get()) {
                        return; // 已优雅完成，读取线程会正常返回结果文件
                    }
                    Thread.sleep(100);
                }
                if (target.isAlive() && !completed.get()) {
                    log.warn("Gacha 进程未响应停止信号，强制终止");
                    target.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * 向子进程所在控制台发送 Ctrl+C 事件。
     * exporter 收到后会设置停止标志，完成收尾并输出 json 结果文件。
     * 通过 JNA AttachConsole 实现；失败时返回 false（调用方会走强制终止兜底）。
     */
    private boolean sendCtrlC(Process target) {
        try {
            // 先脱离当前控制台（若宿主从控制台启动），再挂到子进程的控制台
            Kernel32.INSTANCE.FreeConsole();
            if (!Kernel32.INSTANCE.AttachConsole((int) target.pid())) {
                log.debug("AttachConsole 失败，跳过 Ctrl+C 停止");
                return false;
            }
            // 忽略发送给自身（同控制台）的 Ctrl+C，避免误伤本进程
            Kernel32Ext.INSTANCE.SetConsoleCtrlHandler(null, true);
            boolean ok;
            try {
                ok = Kernel32.INSTANCE.GenerateConsoleCtrlEvent(Wincon.CTRL_C_EVENT, 0);
                log.debug("发送 Ctrl+C 停止信号: {}", ok);
            } finally {
                // 控制台信号异步派发给同控制台所有进程，稍等片刻再恢复自身的 Ctrl+C 处理，
                // 避免本 JVM 在恢复默认处理后收到刚发出的信号
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                Kernel32.INSTANCE.FreeConsole();
                Kernel32Ext.INSTANCE.SetConsoleCtrlHandler(null, false);
            }
            return ok;
        } catch (Throwable t) {
            log.debug("发送 Ctrl+C 失败: {}", t.getMessage());
            return false;
        }
    }

    /** 后台回收进程：最多等待 30 秒，仍未退出则强制终止 */
    private void reapProcessAsync() {
        Process target = this.process;
        if (target == null) {
            return;
        }
        Thread.startVirtualThread(() -> {
            try {
                if (!target.waitFor(30, TimeUnit.SECONDS)) {
                    target.destroyForcibly();
                    log.warn("Gacha 进程超时未退出，已强制终止");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /** 任务被取消/失败时清理残留进程 */
    private void destroyProcessIfRunning() {
        try {
            if (process != null && process.isAlive() && !completed.get()) {
                process.destroyForcibly();
                log.warn("Gacha 进程已被终止");
            }
        } catch (Exception e) {
            log.debug("清理 Gacha 进程失败: {}", e.getMessage());
        }
    }

    @Override
    protected void cancelled() {
        destroyProcessIfRunning();
    }

    @Override
    protected void failed() {
        destroyProcessIfRunning();
    }
}
