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
 * @description: 游戏应用监听器（启动时主动同步 + 运行中纯事件驱动，拒绝常驻线程）
 * @author: Leck
 */
public class GameAppListener implements WinUser.WinEventProc {
    private final Logger LOG = LoggerFactory.getLogger(GameAppListener.class);

    private static final String GAME_WINDOW_TITLE = "异环  ";
    private static GameAppListener gameAppListener;
    private final GameTimeService gameTimeService = AppInjector.getInstance(GameTimeService.class);
    private final User32 user32 = User32.INSTANCE;

    private WinDef.HWND game;
    private WinNT.HANDLE hKey;
    private boolean start = false;
    private LocalDateTime startGameTime;

    private GameAppListener() {}

    public static synchronized GameAppListener getInstance() {
        if (gameAppListener == null) gameAppListener = new GameAppListener();
        return gameAppListener;
    }

    /**
     * 开启监听
     */
    public void startListening() {
        // 1. 挂载前台切换钩子（处理应用运行期间，游戏切换、启动、或关闭后切回其他窗体的事件）
        if (hKey == null) {
            hKey = User32.INSTANCE.SetWinEventHook(0x0003, 0x0003, null, this, 0, 0, 0);
            LOG.info("游戏应用监听器已启动");
        }

        // 2. 【核心改进】启动时单次主动检查：防止游戏早已在前台运行，或者错过了切换事件
        checkGameStatusOnStart();
    }

    /**
     * 启动时单次同步状态，不挂载任何常驻线程
     */
    private void checkGameStatusOnStart() {
        if (start) return;

        // 主动去系统的所有顶级窗口中寻找“异环”
        WinDef.HWND hwnd = user32.FindWindow(null, GAME_WINDOW_TITLE);
        if (hwnd != null && user32.IsWindow(hwnd)) {
            game = hwnd;
            start = true;
            startGameTime = LocalDateTime.now(); // 如果是补录，也可以考虑通过进程创建时间来获取更精准的startGameTime
            LOG.info("【同步成功】检测到游戏已经在前台运行，自动激活监听状态");
        }
    }

    public void stopListening() {
        if (hKey != null) {
            User32.INSTANCE.UnhookWinEvent(hKey);
            hKey = null;
            LOG.info("游戏应用监听器已停止");
        }
        start = false;
    }

    @Override
    public void callback(WinNT.HANDLE handle, WinDef.DWORD dword, WinDef.HWND hwnd, WinDef.LONG aLong, WinDef.LONG aLong1, WinDef.DWORD dword1, WinDef.DWORD dword2) {
        char[] buffer = new char[256];
        user32.GetWindowText(hwnd, buffer, buffer.length);
        String title = Native.toString(buffer);

        if (title.equals(GAME_WINDOW_TITLE)) {
            if (!start) {
                game = hwnd;
                start = true;
                startGameTime = LocalDateTime.now();
                LOG.info("检测到异环切换至前台");
            }
        } else if (title.equals(Config.appTitle)) {
            // 切回本程序时，如果游戏已经启动，更新一次 UI 上的持续时间
            if (start) {
                long duration = java.time.Duration.between(startGameTime, LocalDateTime.now()).toMillis();
                NotificationManager.publish(NotificationKey.HOME_GAME_TIME_UPDATE, duration);
                save(); // 顺便检查游戏是否已经关闭
            }
        } else {
            // 切换到其他无关窗口
            if (start) save();
        }
    }

    private void save() {
        // 纯事件驱动核心：只有在发生窗口切换事件、且游戏窗口已经失效（被关闭）时才触发结算
        if (!user32.IsWindow(game)) {
            start = false;
            LocalDateTime endTime = LocalDateTime.now();

            gameTimeService.saveSession(startGameTime, endTime);
            LOG.info("检测到异环已经结束，记录已保存");

            if (Config.getSetting().isAutoKillOfficialLauncher()){
                LOG.info("游戏结束，自动退出官方启动器");
                new NativeProcessService().killOfficialLauncher();
            }

            if (Config.getSetting().isExitWhenGameOver()){
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
        if (game != null && !user32.IsWindow(game)) {
            game = user32.FindWindow(null, GAME_WINDOW_TITLE);
        }
        return game;
    }
}