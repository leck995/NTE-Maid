package cn.tealc.ntemaid.thread.gacha;

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
 */
public class GachaTask extends Task<File> {
    private static final Logger log = LoggerFactory.getLogger(GachaTask.class);

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

        // 构建命令行
        String exePath = new File("gacha/nte-gacha-exporter-cli.exe").getAbsolutePath();
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
        log.debug("临时文件：{}",completionMarker);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug(line);
                if (line.contains(completionMarker)) {
                    log.debug("抓取结束");
                    completed.set(true);
                    succeeded();
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
     * 手动停止：向进程 stdin 发送 'q'，进程会完成当前页后输出 json=<path> 再退出。
     */
    public void stop() {
        if (writer != null && process != null && process.isAlive()) {
            try {
                writer.write("q\n");
                writer.flush();
            } catch (IOException e) {
                log.error("发送停止指令失败", e);
            }
        }
    }
}
