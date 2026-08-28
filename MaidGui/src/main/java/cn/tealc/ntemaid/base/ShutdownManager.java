package cn.tealc.ntemaid.base;

import cn.tealc.ntemaid.dao.JdbcUtils;
import cn.tealc.ntemaid.jna.GameAppListener;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import cn.tealc.ntemaid.thread.game.log.LogMonitorManager;
import cn.tealc.ntemaid.util.AppLocked;
import cn.tealc.ntemaid.util.SingleInstanceServer;
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
    private SingleInstanceServer ipcServer;

    /**
     * @description: 注入运行期上下文，由 MainApp.start 在启动时调用
     * @param: window 主窗口；appLocked 文件锁；ipcServer 单实例 IPC 服务端
     * @return: void
     * @date:   2026/08/29
     */
    public void setContext(Stage window, AppLocked appLocked, SingleInstanceServer ipcServer) {
        this.window = window;
        this.appLocked = appLocked;
        this.ipcServer = ipcServer;
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
        if (ipcServer != null) {
            ipcServer.stop();
        }
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
