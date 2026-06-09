package cn.tealc.ntemaid.jna;

import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.service.GameTimeService;
import cn.tealc.ntemaid.service.NativeProcessService;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * @program: WutheringWavesTool
 * @description: 有个坑，务必将其设置为全局变量，不知道为什么，不设置为全局变量，钩子无效
 * @author: Leck
 * @create: 2024-07-10 23:29
 */
public class GameAppListener implements WinUser.WinEventProc {
    private final Logger LOG = LoggerFactory.getLogger(GameAppListener.class);
    private static GameAppListener gameAppListener;
    private final GameTimeService gameTimeService = AppInjector.getInstance(GameTimeService.class);
    private WinDef.HWND game;
    private WinNT.HANDLE hKey;
    private boolean start = false;
    private final User32 user32 = User32.INSTANCE;
    private LocalDateTime startGameTime;


    private GameAppListener() {}

    public static GameAppListener getInstance() {
        if (gameAppListener == null) gameAppListener = new GameAppListener();
        return gameAppListener;
    }

    // 提供开启方法
    public void startListening() {
        if (hKey == null) {
            // 0x0003 是 EVENT_SYSTEM_FOREGROUND
            hKey = User32.INSTANCE.SetWinEventHook(0x0003, 0x0003, null, this, 0, 0, 0);
            LOG.info("游戏应用监听器已启动");
        }
    }

    // 提供停止方法
    public void stopListening() {
        if (hKey != null) {
            User32.INSTANCE.UnhookWinEvent(hKey);
            hKey = null;
            LOG.info("游戏应用监听器已停止");
        }
    }

    @Override
    public void callback(WinNT.HANDLE handle, WinDef.DWORD dword, WinDef.HWND hwnd, WinDef.LONG aLong, WinDef.LONG aLong1, WinDef.DWORD dword1, WinDef.DWORD dword2) {
        char[] buffer = new char[256];
        user32.GetWindowText(hwnd, buffer, buffer.length);
        String title = Native.toString(buffer);

        if (title.equals("异环  ")) {
            if (!start) {
                game = hwnd;
                start = true;
                startGameTime = LocalDateTime.now(); // 记录开始时间
                LOG.info("检测到异环已经启动");
            }
        } else if (title.equals(Config.appTitle)) {
            if (start) {
                long duration = java.time.Duration.between(startGameTime, LocalDateTime.now()).toMillis();
                NotificationManager.publish(NotificationKey.HOME_GAME_TIME_UPDATE, duration);
                save();
            }
        } else {
            if (start) save();
        }
    }

    private void save() {
        if (!user32.IsWindow(game)) { // 窗口关闭
            start = false;
            LocalDateTime endTime = LocalDateTime.now();

            gameTimeService.saveSession(startGameTime, endTime);
            LOG.info("检测到异环已经结束，记录已保存");

            if (Config.setting.isAutoKillOfficialLauncher()){
                LOG.info("游戏结束，自动退出官方启动器");
                NativeProcessService service = new NativeProcessService();
                service.killOfficialLauncher();
            }

            if (Config.setting.isExitWhenGameOver()){
                LOG.info("游戏结束，自动退出程序");
                NotificationManager.publish(NotificationKey.APP_EXIT);
            }
        }
    }

    public long getDuration() {
        if (start) {
            return java.time.Duration.between(startGameTime, LocalDateTime.now()).toMillis();
        }
        return 0;
    }

    public WinDef.HWND getGameHWND() {
        return game;
    }
}