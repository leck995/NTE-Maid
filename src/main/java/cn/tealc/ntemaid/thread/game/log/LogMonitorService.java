package cn.tealc.ntemaid.thread.game.log;

import cn.tealc.ntemaid.util.crypto.HTCryptoUtils;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LogMonitorService extends ScheduledService<Void> {

    private final Path logPath;
    private RandomAccessFile raf;
    private long lastKnownPosition = 0;
    private boolean firstRun = true; // 引入标志位，默认为第一次执行
    public LogMonitorService(Path logPath) {
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
                    lastKnownPosition = currentSize; // 第一次检测到文件时，直接把位置移到末尾
                    firstRun = false;               // 标记已跳过历史记录
                    System.out.println("[初始化] 已跳过历史内容，从位置 " + lastKnownPosition + " 开始监听");
                    return; // 结束本次任务，等待下一秒的新增内容
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

                long len = raf.length();
                if (len > lastKnownPosition) {
                    raf.seek(lastKnownPosition);
                    byte[] buffer = new byte[(int) (len - lastKnownPosition)];
                    raf.readFully(buffer);
                    String newChunk = new String(buffer, StandardCharsets.UTF_8);
                    lastKnownPosition = len;

                    // 处理并发送内容
                    processChunk(newChunk);
                }
            }

            private void reopenFile() throws IOException {
                if (raf != null) {
                    try { raf.close(); } catch (IOException ignored) {}
                }
                if (Files.exists(logPath)) {
                    raf = new RandomAccessFile(logPath.toFile(), "r");
                }
            }

            private void processChunk(String chunk) {
                String[] lines = chunk.split("\r?\n", -1);
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    var result = HTCryptoUtils.HTCipher.tryDecryptBase64Line(trimmed);
                    if (result != null) {
                        String decrypted = result.text();
                        // 解密后的文本可能包含换行，需拆开逐行发送
                        String[] subLines = decrypted.split("\n", -1);
                        for (String subLine : subLines) {
                            updateMessage(subLine + "\n");
                        }
                    } else {
                        updateMessage(line + "\n");
                    }
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