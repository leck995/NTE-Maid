package cn.tealc.ntemaid;

import ch.qos.logback.classic.Level;
import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.ShutdownManager;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.jna.GameAppListener;
import cn.tealc.ntemaid.jna.GlobalKeyListener;
import cn.tealc.ntemaid.thread.game.log.LogMonitorManager;
import cn.tealc.ntemaid.thread.system.StartGameTask;
import cn.tealc.ntemaid.ui.tray.TrayIconManager;
import cn.tealc.ntemaid.util.AppLocked;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainApp extends Application {
    private static final Logger LOG = LoggerFactory.getLogger(MainApp.class);
    public static Stage window;

    private ShutdownManager shutdownManager;
    private TrayIconManager trayIconManager;
    private StageInitializer stageInitializer;
    private AppLocked appLocked;

    public MainApp() {
        MvvmFX.setGlobalResourceBundle(Config.language);
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
                .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.toLevel(Config.getSetting().getLogLevel()));
        Platform.setImplicitExit(false);
        appLocked = new AppLocked();
    }

    @Override
    public void start(Stage stage) throws IOException {
        AppInjector.getInjector();
        DependencyInjector.getInstance().setCustomInjector(
                clazz -> AppInjector.getInjector().getInstance(clazz));
        window = stage;

        shutdownManager = AppInjector.getInstance(ShutdownManager.class);
        shutdownManager.setContext(window, appLocked);

        trayIconManager = AppInjector.getInstance(TrayIconManager.class);
        stageInitializer = AppInjector.getInstance(StageInitializer.class);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                LOG.error("检测到未捕获的异常: ", throwable));

        try {
            stageInitializer.init(stage);
            initKeyHook();
            initAppListener();
            initSubscribe();
            trayIconManager.create(stage);
            autoStartGame();
            LogMonitorManager.getInstance().start();
            LOG.info("应用启动成功");
        } catch (Exception e) {
            LOG.error("启动过程中发生严重错误", e);
            shutdownManager.shutdown();
        }
    }

    private void autoStartGame() {
        if (Config.getSetting().isAutoStartGame()) {
            Thread.startVirtualThread(new StartGameTask());
        }
    }

    private void initAppListener() {
        GameAppListener.getInstance().startListening();
    }

    private void initSubscribe() {
        NotificationManager.subscribe(NotificationKey.APP_EXIT,
                ((s, objects) -> shutdownManager.shutdown()));
        NotificationManager.subscribe(NotificationKey.APP_HIDE,
                ((s, objects) -> window.hide()));
    }

    private void initKeyHook() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            throw new RuntimeException(e);
        }
        GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
    }

}
