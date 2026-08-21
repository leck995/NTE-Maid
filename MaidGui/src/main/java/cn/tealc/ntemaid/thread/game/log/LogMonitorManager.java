package cn.tealc.ntemaid.thread.game.log;

import cn.tealc.ntemaid.thread.game.log.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class LogMonitorManager {
    private static final Logger log = LoggerFactory.getLogger(LogMonitorManager.class);
    private static volatile LogMonitorManager instance;

    private final LogMonitorWatchService service;
    private final Path LOG_PATH = Paths.get(System.getenv("LOCALAPPDATA"), "HT/Saved/Logs/HT.log");
    private final List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

    private LogMonitorManager() {
        this.service = new LogMonitorWatchService(LOG_PATH, event -> {
            for (Consumer<String> listener : listeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    log.error("监听器处理事件时发生异常", e);
                }
            }
        });

        // 初始化内置的基础事件订阅者
        addListener(new MusicPlayerEvent());
        addListener(new FishingEvent());
        addListener(new OtherEvent());
        addListener(new PlayerInfoEvent());
        addListener(new BankActivityEvent());
        addListener(new PremiumMonthlyPassEvent());

        // 默认不启动，或者根据需求选择在这里调用 start();
        start();
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

    public void addListener(Consumer<String> listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(Consumer<String> listener) {
        listeners.remove(listener);
    }

    /**
     * 开启/恢复 监听
     */
    public synchronized void start() {
        if (service.isRunning()) {
            log.warn("日志监听服务已经在运行中，请勿重复启动。");
            return;
        }
        log.info("正在启动日志监控虚拟线程...");
        // 关键点：每次启动，都为 Service 创建并开启一个新的虚拟线程
        Thread.startVirtualThread(this.service);
    }

    /**
     * 暂停/停止 监听
     */
    public synchronized void stop() {
        if (service.isRunning()) {
            service.stop();
        } else {
            log.warn("日志监听服务未运行，无需停止。");
        }
    }

    public boolean isRunning() {
        return service.isRunning();
    }
}