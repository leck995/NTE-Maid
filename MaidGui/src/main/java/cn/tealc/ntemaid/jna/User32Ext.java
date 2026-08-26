package cn.tealc.ntemaid.jna;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.win32.W32APIOptions;

/**
 * 统一的 User32 扩展接口，聚合标准库未映射的 Win32 API。
 * 全局单例加载一次，消除各处重复 Native.load("user32") 及重复接口定义。
 */
public interface User32Ext extends User32 {

    /** 全局单例，使用 W32APIOptions.DEFAULT_OPTIONS 保持与标准库一致的 Unicode/ASCII 行为 */
    User32Ext INSTANCE = Native.load("user32", User32Ext.class, W32APIOptions.DEFAULT_OPTIONS);

    /**
     * 将窗口内容渲染到指定设备上下文（用于后台截图）。
     *
     * @param hwnd     目标窗口句柄
     * @param hdcBlt   目标设备上下文
     * @param nFlags   渲染标志（如 PW_CLIENTONLY、PW_RENDERFULLCONTENT）
     * @return 成功返回 true
     */
    boolean PrintWindow(HWND hwnd, HDC hdcBlt, int nFlags);

    /**
     * 向指定窗口的消息队列投递消息（用于后台按键/鼠标注入）。
     *
     * @param hWnd   目标窗口句柄
     * @param Msg    消息类型
     * @param wParam 消息参数
     * @param lParam 消息参数
     * @return 成功返回 true
     */
    boolean PostMessageW(HWND hWnd, int Msg, WPARAM wParam, LPARAM lParam);
}
