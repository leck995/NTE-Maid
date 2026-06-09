package cn.tealc.ntemaid.base;

import cn.tealc.ntemaid.dao.JdbcUtils;
import cn.tealc.ntemaid.jna.GameAppListener;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import cn.tealc.ntemaid.thread.game.log.LogMonitorManager;
import cn.tealc.ntemaid.util.AppLocked;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.google.inject.Singleton;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

@Singleton
public class ShutdownManager {
    private static final Logger LOG = LoggerFactory.getLogger(ShutdownManager.class);

    private Stage window;
    private AppLocked appLocked;

    public void setContext(Stage window, AppLocked appLocked) {
        this.window = window;
        this.appLocked = appLocked;
    }

    public void shutdown() {
        GameAppListener.getInstance().stopListening();

        removeTrayIcons();

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

    private void removeTrayIcons() {
        SystemTray systemTray = SystemTray.getSystemTray();
        for (TrayIcon trayIcon : systemTray.getTrayIcons()) {
            if (trayIcon instanceof cn.tealc.ntemaid.ui.tray.NewFxTrayIcon) {
                systemTray.remove(trayIcon);
            }
        }
    }
}
