package cn.tealc.ntemaid.thread.game.log;

import cn.tealc.ntemaid.thread.game.log.event.FishingEvent;
import cn.tealc.ntemaid.thread.game.log.event.MusicPlayerEvent;
import cn.tealc.ntemaid.thread.game.log.event.OtherEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 日志监控单例管理器
 * 用于协调多个 ViewModel 对游戏日志事件的监听
 */
public class LogMonitorManager {

    private static final Logger log = LoggerFactory.getLogger(LogMonitorManager.class);
    private static volatile LogMonitorManager instance;
    private final LogMonitorForMusicTask service;
    private final Path LOG_PATH = Paths.get(System.getenv("LOCALAPPDATA"), "HT/Saved/Logs/HT.log");
    
    // 使用线程安全的 List 存储所有订阅者
    private final List<Consumer<LogMonitorForMusicTask.Event>> listeners = new CopyOnWriteArrayList<>();

    private LogMonitorManager() {
        this.service = new LogMonitorForMusicTask(LOG_PATH);
        // 核心：在 Service 内部检测到事件时，广播给所有订阅者
        this.service.setOnEventDetected(event -> {
            for (Consumer<LogMonitorForMusicTask.Event> listener : listeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    // 防止某个监听器报错导致后续监听器失效
                    log.error("LogMonitorManager",e);
                }
            }
        });
        addListener(new MusicPlayerEvent());
        addListener(new FishingEvent());
        addListener(new OtherEvent());

    }

    public static LogMonitorManager getInstance() {
        if (instance == null) {
            synchronized (LogMonitorManager.class) {
                if (instance == null) {
                    instance = new LogMonitorManager();
                }
            }
        }
        return instance;
    }

    /**
     * 添加订阅者
     */
    public void addListener(Consumer<LogMonitorForMusicTask.Event> listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * 移除订阅者（防止内存泄漏）
     */
    public void removeListener(Consumer<LogMonitorForMusicTask.Event> listener) {
        listeners.remove(listener);
    }

    public void start() {
        if (!service.isRunning()) {
            service.restart();
        }
    }

    public void stop() {
        service.cancel();
    }
    
    public boolean isRunning() {
        return service.isRunning();
    }








}