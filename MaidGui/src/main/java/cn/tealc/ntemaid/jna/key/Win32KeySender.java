package cn.tealc.ntemaid.jna.key;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.jna.GameAppListener;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 通过 Win32 PostMessage API 向游戏窗口发送键盘/鼠标事件，支持后台窗口
 */
public class Win32KeySender {
    private static final Logger log = LoggerFactory.getLogger(Win32KeySender.class);
    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "key-sender");
                t.setDaemon(true);
                return t;
            });

    private static final int WM_LBUTTONDOWN = 0x0201;
    private static final int WM_LBUTTONUP = 0x0202;
    private static final int WM_RBUTTONDOWN = 0x0204;
    private static final int WM_RBUTTONUP = 0x0205;

    private HWND hwnd;

    public Win32KeySender() {
        reGetHwnd();
    }

    public void reGetHwnd() {
        this.hwnd = GameAppListener.getInstance().getGameHWND();
        if (this.hwnd == null) {
            log.warn("未找到游戏窗口: {}", Config.getSetting().getGameWindowTitles());
        }
    }

    public HWND getGameHwnd() {
        return hwnd;
    }

    // ---- 键盘 ----

    public void clickKey(VirtualKey key) {
        postKey(key.vkCode, key.scanCode);
    }

    public void clickKey(VirtualKey key, Duration delay) {
        scheduler.schedule(() -> clickKey(key), (long) delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void pressKey(VirtualKey key) {
        pressKey(key.vkCode, key.scanCode);
    }
    public void releaseKey(VirtualKey key) {
        releaseKey(key.vkCode, key.scanCode);
    }

    // ---- 鼠标：立即执行 ----

    /**
     * 在目标窗口的客户区坐标 (x, y) 处模拟鼠标左键点击
     */
    public void clickLeft(int x, int y) {
        postMouseClick(WM_LBUTTONDOWN, WM_LBUTTONUP, x, y);
    }

    /**
     * 在目标窗口的客户区坐标 (x, y) 处模拟鼠标右键点击
     */
    public void clickRight(int x, int y) {
        postMouseClick(WM_RBUTTONDOWN, WM_RBUTTONUP, x, y);
    }

    // ---- 鼠标：延时执行 ----

    public void clickLeft(int x, int y, Duration delay) {
        scheduler.schedule(() -> clickLeft(x, y), (long) delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void clickRight(int x, int y, Duration delay) {
        scheduler.schedule(() -> clickRight(x, y), (long) delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    // ---- 内部实现 ----

    private void postKey(int vkCode, int scanCode) {
        if (hwnd == null) {
            return;
        }
        int lParamDown = (scanCode << 16);
        int lParamUp = (scanCode << 16) | (1 << 30) | (1 << 31) | 1;
        User32Ext.INSTANCE.PostMessageW(hwnd, WinUser.WM_KEYDOWN, new WPARAM(vkCode), new LPARAM(lParamDown));
        User32Ext.INSTANCE.PostMessageW(hwnd, WinUser.WM_KEYUP, new WPARAM(vkCode), new LPARAM(lParamUp));
        log.debug("PostMessage 按键 VK=0x{} SC=0x{} -> {}", Integer.toHexString(vkCode), Integer.toHexString(scanCode), Config.getSetting().getGameWindowTitles());
    }

    private void pressKey(int vkCode, int scanCode) {
        if (hwnd == null) {
            return;
        }
        int lParamDown = (scanCode << 16);
        User32Ext.INSTANCE.PostMessageW(hwnd, WinUser.WM_KEYDOWN, new WPARAM(vkCode), new LPARAM(lParamDown));
    }
    private void releaseKey(int vkCode, int scanCode) {
        if (hwnd == null) {
            return;
        }
        int lParamUp = (scanCode << 16) | (1 << 30) | (1 << 31) | 1;
        User32Ext.INSTANCE.PostMessageW(hwnd, WinUser.WM_KEYUP, new WPARAM(vkCode), new LPARAM(lParamUp));}



    private void postMouseClick(int downMsg, int upMsg, int x, int y) {
        if (hwnd == null) {
            return;
        }

        int lParam = (y << 16) | (x & 0xFFFF);

        User32Ext.INSTANCE.PostMessageW(hwnd, downMsg, new WPARAM(0), new LPARAM(lParam));
        User32Ext.INSTANCE.PostMessageW(hwnd, upMsg, new WPARAM(0), new LPARAM(lParam));
        log.debug("PostMessage 鼠标点击 ({}, {}) -> {}", x, y, Config.getSetting().getGameWindowTitles());
    }

    // ---- VirtualKey 枚举 ----

    public enum VirtualKey {
        ESC(0x1B, 0x01),
        DIGIT0(0x30, 0x0B),
        DIGIT1(0x31, 0x02),
        DIGIT2(0x32, 0x03),
        DIGIT3(0x33, 0x04),
        DIGIT4(0x34, 0x05),
        DIGIT5(0x35, 0x06),
        DIGIT6(0x36, 0x07),
        DIGIT7(0x37, 0x08),
        DIGIT8(0x38, 0x09),
        DIGIT9(0x39, 0x0A),
        A(0x41, 0x1E),
        B(0x42, 0x30),
        C(0x43, 0x2E),
        D(0x44, 0x20),
        E(0x45, 0x12),
        F(0x46, 0x21),
        G(0x47, 0x22),
        H(0x48, 0x23),
        I(0x49, 0x17),
        J(0x4A, 0x24),
        K(0x4B, 0x25),
        L(0x4C, 0x26),
        M(0x4D, 0x32),
        N(0x4E, 0x31),
        O(0x4F, 0x18),
        P(0x50, 0x19),
        Q(0x51, 0x10),
        R(0x52, 0x13),
        S(0x53, 0x1F),
        T(0x54, 0x14),
        U(0x55, 0x16),
        V(0x56, 0x2F),
        W(0x57, 0x11),
        X(0x58, 0x2D),
        Y(0x59, 0x15),
        Z(0x5A, 0x2C),
        F1(0x70, 0x3B),
        F2(0x71, 0x3C),
        F3(0x72, 0x3D),
        F4(0x73, 0x3E),
        F5(0x74, 0x3F),
        F6(0x75, 0x40),
        F7(0x76, 0x41),
        F8(0x77, 0x42),
        F9(0x78, 0x43),
        F10(0x79, 0x44),
        F11(0x7A, 0x57),
        F12(0x7B, 0x58),
        SPACE(0x20, 0x39),
        ENTER(0x0D, 0x1C),
        TAB(0x09, 0x0F),
        BACKSPACE(0x08, 0x0E),
        LEFT(0x25, 0x4B),
        UP(0x26, 0x48),
        RIGHT(0x27, 0x4D),
        DOWN(0x28, 0x50),
        PLUS(0xBB, 0x0D),
        MINUS(0xBD, 0x0C);

        public final int vkCode;
        public final int scanCode;

        VirtualKey(int vkCode, int scanCode) {
            this.vkCode = vkCode;
            this.scanCode = scanCode;
        }
    }

    private interface User32Ext extends StdCallLibrary {
        User32Ext INSTANCE = Native.load("user32", User32Ext.class);

        HWND FindWindowW(String lpClassName, String lpWindowName);

        boolean PostMessageW(HWND hWnd, int Msg, WPARAM wParam, LPARAM lParam);
    }
}
