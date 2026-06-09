package cn.tealc.ntemaid.thread.game.log;

import cn.tealc.ntemaid.util.crypto.HTCryptoUtils;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LogMonitorTask extends ScheduledService<Void> {

    private final Path logPath;
    private RandomAccessFile raf;
    private long lastKnownPosition = 0;
    private boolean firstRun = true;
    private String partialLine = null; // 缓冲区：上一次读取被截断的不完整行
    public LogMonitorTask(Path logPath) {
        this.logPath = logPath;
        setPeriod(Duration.seconds(1));
        //setDelay(Duration.seconds(1));
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 每次执行时调用外部类的监控方法
                checkAndReadNewLines();
                return null;
            }

            /**
             * 在 Task 内部定义方法，可以调用 updateMessage
             */
            private void checkAndReadNewLines() throws IOException {
                if (logPath == null || !Files.exists(logPath)) return;

                long currentSize = Files.size(logPath);
                if (firstRun) {
                    lastKnownPosition = currentSize;
                    firstRun = false;
                    System.out.println("[初始化] 已跳过历史内容，从位置 " + lastKnownPosition + " 开始监听");
                    return;
                }

                // 文件轮转检测：新文件尺寸小于上次读取位置
                if (currentSize < lastKnownPosition) {
                    reopenFile();
                    lastKnownPosition = 0;
                    System.out.println("[日志轮转] 检测到新文件，重置读取位置");
                    updateMessage("[日志轮转] 检测到新文件，重置读取位置\n");
                }

                if (raf == null) {
                    reopenFile();
                }
                if (raf == null) return;

                // 统一使用 currentSize 而非 raf.length()，避免 handle 缓存导致的不一致
                if (currentSize > lastKnownPosition) {
                    raf.seek(lastKnownPosition);
                    byte[] buffer = new byte[(int) (currentSize - lastKnownPosition)];
                    raf.readFully(buffer);
                    String newChunk = new String(buffer, StandardCharsets.UTF_8);
                    lastKnownPosition = currentSize;
                    System.out.println(newChunk);
                    processChunk(newChunk);
                }
            }

            private void reopenFile() throws IOException {
                partialLine = null; // 文件轮转后旧缓冲区失效
                if (raf != null) {
                    try { raf.close(); } catch (IOException ignored) {}
                }
                if (Files.exists(logPath)) {
                    raf = new RandomAccessFile(logPath.toFile(), "r");
                }
            }

            private void processChunk(String chunk) {
                // 拼接上一次被截断的不完整行
                if (partialLine != null) {
                    chunk = partialLine + chunk;
                    partialLine = null;
                }

                boolean endsWithNewline = chunk.endsWith("\n") || chunk.endsWith("\r");
                String[] lines = chunk.split("\r?\n", -1);
                int endIndex = endsWithNewline ? lines.length : lines.length - 1;

                for (int i = 0; i < endIndex; i++) {
                    String line = lines[i];
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    var result = HTCryptoUtils.HTCipher.tryDecryptBase64Line(trimmed);
                    if (result != null) {
                        String decrypted = result.text();
                        String[] subLines = decrypted.split("\n", -1);
                        for (String subLine : subLines) {
                            updateMessage(subLine + "\n");
                        }
                    } else {
                        updateMessage(line + "\n");
                    }
                }

                // 保存不完整的最后一行，等待下次拼接
                if (!endsWithNewline && lines.length > 0) {
                    partialLine = lines[lines.length - 1];
                }
            }
        };
    }

    /**
     * 覆盖 cancel 方法，返回 boolean 并关闭资源
     */
    @Override
    public boolean cancel() {
        boolean cancelled = super.cancel();
        closeRandomAccessFile();
        return cancelled;
    }

    private synchronized void closeRandomAccessFile() {
        if (raf != null) {
            try {
                raf.close();
            } catch (IOException ignored) {
            }
            raf = null;
        }
    }
}