package cn.tealc.ntemaid.snapshot;

import cn.tealc.ntemaid.jna.GameAppListener;
import cn.tealc.ntemaid.jna.User32Ext;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class WindowCapture {

    private static final User32 user32 = User32.INSTANCE;
    private static final GDI32 gdi32 = GDI32.INSTANCE;
    private static final User32Ext user32Ext = User32Ext.INSTANCE;
    // Windows API 常量定义
    private static final int PW_CLIENTONLY = 0x00000001;       // 只截取客户区（不要标题栏和边框）
    private static final int PW_RENDERFULLCONTENT = 0x00000002; // 确保硬件加速内容不黑屏

    /**
     * 仅截取游戏客户区（去除标题栏和边框）
     */
    public static BufferedImage captureGameClientArea(HWND hwnd) {
        // 【修改点 1】使用 GetClientRect 代替 GetWindowRect
        // GetClientRect 拿到的 rect.left 和 rect.top 永远是 0
        // rect.right 和 rect.bottom 就是游戏画面的纯净分辨率
        RECT rect = new RECT();
        user32.GetClientRect(hwnd, rect);
        int width = rect.right;
        int height = rect.bottom;

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("游戏窗口未完全加载或大小为0");
        }

        // 创建DC
        HDC hdcWindow = user32.GetDC(hwnd);
        HDC hdcMem = gdi32.CreateCompatibleDC(hdcWindow);
        HBITMAP hBitmap = gdi32.CreateCompatibleBitmap(hdcWindow, width, height);
        WinNT.HANDLE oldBitmap = gdi32.SelectObject(hdcMem, hBitmap);

        // 【修改点 2】组合 Flags
        // 将 PW_CLIENTONLY(1) 和 PW_RENDERFULLCONTENT(2) 进行按位或(|)运算，结果为 3
        int flags = PW_CLIENTONLY | PW_RENDERFULLCONTENT;
        boolean success = user32Ext.PrintWindow(hwnd, hdcMem, flags);

        if (!success) {
            cleanup(hdcWindow, hdcMem, hBitmap, oldBitmap, hwnd);
            throw new RuntimeException("PrintWindow 失败，错误码: " + Kernel32.INSTANCE.GetLastError());
        }

        // 读取像素数据
        BITMAPINFO bmi = new BITMAPINFO();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height; // 负数表示自上而下的位图
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        Memory buffer = new Memory((long) width * height * 4);
        gdi32.GetDIBits(hdcMem, hBitmap, 0, height, buffer, bmi, WinGDI.DIB_RGB_COLORS);

        // 转换图像
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] pixels = buffer.getIntArray(0, width * height);
        image.setRGB(0, 0, width, height, pixels, 0, width);

        cleanup(hdcWindow, hdcMem, hBitmap, oldBitmap, hwnd);
        return image;
    }

    private static void cleanup(HDC hdcWindow, HDC hdcMem, HBITMAP hBitmap,
                                WinNT.HANDLE oldBitmap, HWND hwnd) {
        if (oldBitmap != null) gdi32.SelectObject(hdcMem, oldBitmap);
        if (hBitmap != null) gdi32.DeleteObject(hBitmap);
        if (hdcMem != null) gdi32.DeleteDC(hdcMem);
        if (hdcWindow != null) user32.ReleaseDC(hwnd, hdcWindow);
    }

    public static void snapshot(String[] args) {
        // 复用 GameAppListener 统一封装的窗口查找逻辑
        HWND hwnd = GameAppListener.getInstance().findGameWindow();

        if (hwnd == null) {
            System.out.println("未找到窗口，请确认游戏已启动且窗口标题正确。");
            return;
        }

        long start = System.currentTimeMillis();
        BufferedImage screenshot = captureGameClientArea(hwnd);
        System.out.println("截图用时: " + (System.currentTimeMillis() - start) + "ms");

        try {
            ImageIO.write(screenshot, "png", new File("异环纯画面截图.png"));
            System.out.println("保存成功，纯画面分辨率: " + screenshot.getWidth() + "x" + screenshot.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}