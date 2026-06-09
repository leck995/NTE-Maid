package cn.tealc.ntemaid.thread.game.log;

import cn.tealc.ntemaid.util.crypto.HTCryptoUtils;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.function.Consumer;

public class LogMonitorWatchService implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(LogMonitorWatchService.class);

    private final Path logPath;
    private final Consumer<String> onEventDetected;

    // 使用 volatile 保证线程间可见
    private volatile boolean running = false;
    private WatchService watchService;
    private RandomAccessFile raf;
    private long lastKnownPosition = 0;
    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

    public LogMonitorWatchService(Path logPath, Consumer<String> onEventDetected) {
        this.logPath = logPath;
        this.onEventDetected = onEventDetected;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * 显式停止方法
     */
    public void stop() {
        if (!running) return;
        this.running = false;
        log.info("正在关闭日志监听服务...");

        // 关键：关闭 WatchService 会使底层阻塞的 watchService.take() 抛出 ClosedWatchServiceException
        // 从而瞬间打破阻塞，让线程安全退出
        if (watchService != null) {
            try { watchService.close(); } catch (IOException ignored) {}
        }
        closeRandomAccessFile();
    }

    @Override
    public void run() {
        if (logPath == null) return;
        Path parentDir = logPath.getParent();
        if (parentDir == null) return;

        this.running = true;

        try {
            // 每次运行都实例化全新的 WatchService
            this.watchService = FileSystems.getDefault().newWatchService();
            parentDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            // 初始化文件位置（跳过历史）
            if (Files.exists(logPath)) {
                lastKnownPosition = Files.size(logPath);
                raf = new RandomAccessFile(logPath.toFile(), "r");
            }

            log.info("NIO WatchService 日志监控虚拟线程已成功启动...");

            while (running) {
                // 如果 watchService 被关闭，这里会抛出 ClosedWatchServiceException
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;

                    Path context = (Path) event.context();
                    if (logPath.getFileName().equals(context)) {
                        readAvailableLines();
                    }
                }

                if (!key.reset()) {
                    log.warn("WatchKey 失效，目录可能已被删除");
                    break;
                }
            }
        } catch (ClosedWatchServiceException e) {
            log.info("WatchService 已主动关闭。");
        } catch (InterruptedException e) {
            log.info("日志监控线程被中断请求终止。");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("日志监控运行中发生未知异常: ", e);
        } finally {
            this.running = false;
            closeRandomAccessFile();
            log.info("日志监控后端线程已彻底安全退出。");
        }
    }

    // readAvailableLines, processBytes, handleLine 等方法保持不变...
    private void readAvailableLines() {
        try {
            if (!Files.exists(logPath)) return;
            long currentSize = Files.size(logPath);
            if (currentSize < lastKnownPosition) {
                lineBuffer.reset();
                if (raf != null) raf.close();
                raf = new RandomAccessFile(logPath.toFile(), "r");
                lastKnownPosition = 0;
            }
            if (raf == null) raf = new RandomAccessFile(logPath.toFile(), "r");

            if (currentSize > lastKnownPosition) {
                raf.seek(lastKnownPosition);
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalRead = 0;
                long maxToRead = currentSize - lastKnownPosition;

                while (totalRead < maxToRead &&
                        (bytesRead = raf.read(buffer, 0, (int) Math.min(buffer.length, maxToRead - totalRead))) != -1) {
                    processBytes(buffer, bytesRead);
                    totalRead += bytesRead;
                }
                lastKnownPosition = raf.getFilePointer();
            }
        } catch (IOException e) {
            log.error("读取日志发生错误", e);
        }
    }

    private void processBytes(byte[] bytes, int len) {
        for (int i = 0; i < len; i++) {
            byte b = bytes[i];
            if (b == '\n') { handleLine(); }
            else if (b == '\r') {
                handleLine();
                if (i + 1 < len && bytes[i + 1] == '\n') i++;
            } else { lineBuffer.write(b); }
        }
    }

    private void handleLine() {
        if (lineBuffer.size() == 0) return;
        String line = lineBuffer.toString(StandardCharsets.UTF_8).trim();
        lineBuffer.reset();
        if (line.isEmpty()) return;

        var result = HTCryptoUtils.HTCipher.tryDecryptBase64Line(line);
        if (result != null) {
            if (onEventDetected != null) onEventDetected.accept(result.text());
        }
    }

    private void closeRandomAccessFile() {
        if (raf != null) {
            try { raf.close(); } catch (IOException ignored) {}
            raf = null;
        }
    }
}