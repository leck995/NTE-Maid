package cn.tealc.ntemaid.thread.game.log;

import cn.tealc.ntemaid.util.crypto.HTCryptoUtils;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class LogMonitorForMusicService extends ScheduledService<LogMonitorForMusicService.Event> {
    private static final Logger log = LoggerFactory.getLogger(LogMonitorForMusicService.class);
    private final Path logPath;
    private RandomAccessFile raf;
    private long lastKnownPosition = 0;
    private boolean firstRun = true; // 引入标志位，默认为第一次执行
    private static final String ON_VEHICLE = "End Get On Vehicle";
    private static final String Off_VEHICLE = "Start Get Off Vehicle";
    private static final String MUSIC_PLAYING = "UHTSoundSubsystem UHTUI_Vehicle::OnPlayOrPauseBtnCallBack ScrollMusicTitle.isValid = [1], bChecked = [1]";
    private static final String MUSIC_PAUSE = "UHTSoundSubsystem UHTUI_Vehicle::OnPlayOrPauseBtnCallBack ScrollMusicTitle.isValid = [1], bChecked = [0]";
    private static final String BEGIN_TRANSFER = "LevelTransferState BeginTransfer";
    private static final String ENDPLAY_RACING = "EndPlay_Racing LEVEL_TYPE_RACING_PVP";
    public LogMonitorForMusicService(Path logPath) {
        this.logPath = logPath;
        setPeriod(Duration.seconds(1));
    }
    private Consumer<Event> onEventDetected;
    public void setOnEventDetected(Consumer<Event> handler) { this.onEventDetected = handler; }

    @Override
    protected Task<LogMonitorForMusicService.Event> createTask() {
        return new Task<>() {
            @Override
            protected LogMonitorForMusicService.Event call() throws Exception {
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
                        //System.out.println(decrypted);
                        if (decrypted.contains(MUSIC_PAUSE)) {
                            log.debug("检测到游戏内置播放器暂停音乐");
                            //player.play();
                            Platform.runLater(() -> {
                                if(onEventDetected != null) onEventDetected.accept(Event.MUSIC_PAUSE);
                            });
                        }else if (decrypted.contains(MUSIC_PLAYING)) {
                            log.debug("检测到游戏内置播放器播放音乐");
                            //player.pause();
                            Platform.runLater(() -> {
                                if(onEventDetected != null) onEventDetected.accept(Event.MUSIC_PLAYING);
                            });
                        } else if (decrypted.contains(Off_VEHICLE)) {
                            log.debug("检测到玩家下车");
                            Platform.runLater(() -> {
                                if(onEventDetected != null) onEventDetected.accept(Event.Off_VEHICLE);
                            });
                        } else if (decrypted.contains(BEGIN_TRANSFER)) {
                            log.debug("检测到玩家传送");
                            Platform.runLater(() -> {
                                if(onEventDetected != null) onEventDetected.accept(Event.BEGIN_TRANSFER);
                            });
                        } else if (decrypted.contains(ENDPLAY_RACING)) {
                            log.debug("检测到玩家结束赛车比赛");
                            Platform.runLater(() -> {
                                if(onEventDetected != null) onEventDetected.accept(Event.ENDPLAY_RACING);
                            });
                        } else if (decrypted.contains(ON_VEHICLE)) {
                            log.debug("检测到玩家上车");
                            Platform.runLater(() -> {
                                if(onEventDetected != null) onEventDetected.accept(Event.ON_VEHICLE);
                            });
                        }
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


    /**
     * 日志事件枚举类
     *
     * @author leck
     * @date 2026/05/05
     */
    public enum Event{
        ON_VEHICLE,
        Off_VEHICLE,
        MUSIC_PLAYING,
        MUSIC_PAUSE,
        BEGIN_TRANSFER,
        ENDPLAY_RACING //在线赛车结束
    }
}