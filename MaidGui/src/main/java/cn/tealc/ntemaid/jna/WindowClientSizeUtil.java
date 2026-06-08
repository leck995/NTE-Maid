package cn.tealc.ntemaid.jna;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser;
import javafx.geometry.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 根据 HWND 获取窗口客户区（不含标题栏和边框）的宽高
 */
public final class WindowClientSizeUtil {
    private static final Logger log = LoggerFactory.getLogger(WindowClientSizeUtil.class);

    private WindowClientSizeUtil() {}

    public static int getWidth(HWND hwnd) {
        RECT rect = getClientRect(hwnd);
        return rect.right - rect.left;
    }

    public static int getHeight(HWND hwnd) {
        RECT rect = getClientRect(hwnd);
        return rect.bottom - rect.top;
    }

    /**
     * @return Point2D，x = 宽度，y = 高度
     */
    public static Point2D getSize(HWND hwnd) {
        RECT rect = getClientRect(hwnd);
        return new Point2D(rect.right - rect.left, rect.bottom - rect.top);
    }

    private static RECT getClientRect(HWND hwnd) {
        RECT rect = new RECT();
        if (hwnd == null) {
            log.warn("HWND 为 null，无法获取窗口客户区大小");
            return rect;
        }
        if (!User32.INSTANCE.GetClientRect(hwnd, rect)) {
            log.warn("GetClientRect 调用失败，HWND 可能已失效");
            return rect;
        }

        if (rect.right == 0 && rect.bottom == 0) {
            WinUser.WINDOWPLACEMENT wp = new WinUser.WINDOWPLACEMENT();
            if (User32.INSTANCE.GetWindowPlacement(hwnd, wp).booleanValue()
                    && wp.showCmd == WinUser.SW_SHOWMINIMIZED) {
                log.debug("窗口已最小化，使用 rcNormalPosition 推算客户区大小");
                RECT normal = wp.rcNormalPosition;
                int frameW = User32.INSTANCE.GetSystemMetrics(WinUser.SM_CXSIZEFRAME);
                int frameH = User32.INSTANCE.GetSystemMetrics(WinUser.SM_CYSIZEFRAME);
                int captionH = User32.INSTANCE.GetSystemMetrics(WinUser.SM_CYCAPTION);
                rect.right = normal.right - normal.left - frameW * 2;
                rect.bottom = normal.bottom - normal.top - captionH - frameH * 2;
            }
        }

        return rect;
    }
}
