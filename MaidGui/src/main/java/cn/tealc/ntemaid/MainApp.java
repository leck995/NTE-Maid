package cn.tealc.ntemaid;

import ch.qos.logback.classic.Level;
import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.dao.JdbcUtils;
import cn.tealc.ntemaid.jna.GameAppListener;
import cn.tealc.ntemaid.jna.GlobalKeyListener;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import cn.tealc.ntemaid.thread.game.log.LogMonitorManager;
import cn.tealc.ntemaid.thread.system.StartGameTask;
import cn.tealc.ntemaid.ui.system.MainView;
import cn.tealc.ntemaid.ui.system.MainViewModel;
import cn.tealc.ntemaid.ui.tray.NewFxTrayIcon;
import cn.tealc.ntemaid.util.AppLocked;
import cn.tealc.ntemaid.util.LanguageManager;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;

public class MainApp extends Application {
    private static final Logger LOG = LoggerFactory.getLogger(MainApp.class);
    public static Stage window;
    private static AppLocked appLocked;
    private NewFxTrayIcon newFxTrayIcon;

    public MainApp() {
        MvvmFX.setGlobalResourceBundle(Config.language);
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
                .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.toLevel(Config.setting.getLogLevel()));
        Platform.setImplicitExit(false);
        appLocked = new AppLocked();
    }

    @Override
    public void start(Stage stage) throws IOException {
        AppInjector.getInjector();
        window = stage;
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LOG.error("检测到未捕获的异常: ", throwable);
        });
        try {
            initStage(stage);
            //initKeyHook();
            initAppListener();
            initSubscribe();
            createTrayIcon();
            autoStartGame();

            startLogMonitor();

            LOG.info("应用启动成功");
        } catch (Exception e) {
            LOG.error("启动过程中发生严重错误", e);
            exit();
        }
    }

    private void startLogMonitor() {
        LogMonitorManager.getInstance().start();
    }

    private void autoStartGame() {
        if (Config.setting.isAutoStartGame()) {
            Thread.startVirtualThread(new StartGameTask());
        }
    }

    private void initStage(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(FXResourcesLoader.load("css/light.css"));
        ViewTuple<MainView, MainViewModel> viewTuple = FluentViewLoader.fxmlView(MainView.class).load();
        Scene scene = new Scene(viewTuple.getView());
        scene.getStylesheets().add(FXResourcesLoader.load("css/Default.css"));

        stage.setScene(scene);
        stage.getIcons().add(new Image(FXResourcesLoader.load("image/icon.png"), 45, 45, true, true));
        stage.setTitle(LanguageManager.getString("app.title"));
        double width = Math.max(1100, Config.setting.getAppWidth());
        double height = Math.max(650, Config.setting.getAppHeight());
        stage.setWidth(width);
        stage.setHeight(height);

        Config.setting.appWidthProperty().bind(scene.widthProperty());
        Config.setting.appHeightProperty().bind(scene.heightProperty());
        stage.initStyle(StageStyle.EXTENDED);
        initFont();
        if (!Config.setting.isSilentStartup()) {
            stage.show();
        }
    }

    private void initAppListener() {
        GameAppListener.getInstance().startListening();
    }

    private static void initSubscribe() {
        NotificationManager.subscribe(NotificationKey.APP_EXIT, ((s, objects) -> {
            exit();
        }));
        NotificationManager.subscribe(NotificationKey.APP_HIDE, ((s, objects) -> {
            hide();
        }));
    }

    private void initFont() {
        Font.loadFonts(FXResourcesLoader.loadStream("font/HarmonyOS_Sans_SC_Bold.ttf"), 12);
        window.getScene().getRoot().setStyle("-fx-font-family: \"HarmonyOS Sans SC\"");
    }

    public void initKeyHook() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            throw new RuntimeException(e);
        }
        GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
    }


    public static void exit() {
        GameAppListener.getInstance().stopListening();
        SystemTray systemTray = SystemTray.getSystemTray();
        for (TrayIcon trayIcon : systemTray.getTrayIcons()) {
            if (trayIcon instanceof NewFxTrayIcon tray) {
                systemTray.remove(tray);
            }
        }
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            LOG.error("卸载键盘钩子失败", e);
        }
        Platform.setImplicitExit(true);
        window.setX(-10000);
        window.setMaximized(false);
        appLocked.release();
        MusicPlayerClient.getInstance().close();
        LogMonitorManager.getInstance().stop();
        Config.save();
        JdbcUtils.exit();
        window.close();
        System.exit(0);
    }

    public static void hide() {
        window.hide();
    }



    private void createTrayIcon() {
        if (SystemTray.isSupported()) {
            javafx.scene.control.Button show = new javafx.scene.control.Button(LanguageManager.getString("ui.tray.show"), new FontIcon(Material2OutlinedMZ.REMOVE_FROM_QUEUE));
            show.setOnAction(event -> {
                window.setIconified(false);
                window.show();
                window.toFront();
            });
            javafx.scene.control.Button exit = new Button(LanguageManager.getString("ui.tray.exit"), new FontIcon(Material2OutlinedMZ.POWER_SETTINGS_NEW));
            exit.setOnAction(event -> Platform.runLater(MainApp::exit));
            VBox vbox = new VBox(show, exit);
            vbox.getStyleClass().add("tray");
            vbox.getStylesheets().add(FXResourcesLoader.load("css/TrayIcon.css"));
            vbox.setPrefWidth(80);
            vbox.setPrefHeight(60);
            newFxTrayIcon = new NewFxTrayIcon(SwingFXUtils.fromFXImage(window.getIcons().getFirst(), null), Config.appTitle, vbox);
            newFxTrayIcon.addActionListener(e -> {
                Platform.runLater(() -> {
                    window.setIconified(false);
                    window.show();
                    window.toFront();
                });
            });
            SystemTray systemTray = SystemTray.getSystemTray();
            try {
                systemTray.add(newFxTrayIcon);
            } catch (AWTException e) {
                LOG.error("Tray Error", e);
            }
        } else {
            LOG.info("SystemTray is not supported");
        }
    }
}

