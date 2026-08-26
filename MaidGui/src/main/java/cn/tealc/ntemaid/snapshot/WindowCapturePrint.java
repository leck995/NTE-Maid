package cn.tealc.ntemaid.snapshot;

import cn.tealc.ntemaid.base.Config;
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

public class WindowCapturePrint {

    private static final User32Ext user32 = User32Ext.INSTANCE;
    private static final GDI32 gdi32 = GDI32.INSTANCE;

    public static BufferedImage captureWindow(HWND hwnd) {
        // 1. 获取窗口矩形（注意：PrintWindow 建议使用 GetWindowRect 确保包含边框，
        // 或者使用 GetClientRect 仅抓取客户区内容）
        RECT rect = new RECT();
        user32.GetWindowRect(hwnd, rect);
        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("窗口不可见或大小为0");
        }

        // 2. 创建内存 DC 和位图
        HDC hdcWindow = user32.GetDC(hwnd);
        HDC hdcMem = gdi32.CreateCompatibleDC(hdcWindow);
        HBITMAP hBitmap = gdi32.CreateCompatibleBitmap(hdcWindow, width, height);
        WinNT.HANDLE oldBitmap = gdi32.SelectObject(hdcMem, hBitmap);

        try {
            // 3. 使用 PrintWindow 抓取内容
            // 标志位 2 (PW_RENDERFULLCONTENT) 是解决黑屏的关键，能抓取大部分硬件加速内容
            boolean success = user32.PrintWindow(hwnd, hdcMem, 2);

            if (!success) {
                // 如果设置 2 失败，尝试设置 0 (兼容旧系统或特定窗口)
                success = user32.PrintWindow(hwnd, hdcMem, 0);
            }

            if (!success) {
                throw new RuntimeException("PrintWindow 失败");
            }

            // 4. 将位图转换为 BufferedImage
            BITMAPINFO bmi = new BITMAPINFO();
            bmi.bmiHeader.biWidth = width;
            bmi.bmiHeader.biHeight = -height; // 坐标倒置
            bmi.bmiHeader.biPlanes = 1;
            bmi.bmiHeader.biBitCount = 32;
            bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

            Memory buffer = new Memory((long) width * height * 4);
            int result = gdi32.GetDIBits(hdcMem, hBitmap, 0, height, buffer, bmi, WinGDI.DIB_RGB_COLORS);

            if (result == 0) {
                throw new RuntimeException("GetDIBits 失败");
            }

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int[] pixels = buffer.getIntArray(0, width * height);
            image.setRGB(0, 0, width, height, pixels, 0, width);

            return image;

        } finally {
            // 5. 彻底释放资源
            if (oldBitmap != null) gdi32.SelectObject(hdcMem, oldBitmap);
            if (hBitmap != null) gdi32.DeleteObject(hBitmap);
            if (hdcMem != null) gdi32.DeleteDC(hdcMem);
            if (hdcWindow != null) user32.ReleaseDC(hwnd, hdcWindow);
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        // 复用 GameAppListener 统一封装的窗口查找逻辑
        HWND hwnd = GameAppListener.getInstance().findGameWindow();

        if (hwnd == null) {
            System.err.println("找不到窗口，请检查配置的标题是否包含多余空格: " + Config.getSetting().getGameWindowTitles());
            return;
        }

        try {
            System.out.println("正在截取...");
            BufferedImage img = captureWindow(hwnd);
            long end = System.currentTimeMillis();
            System.out.println(end - start);
            File output = new File("captured.png");
            ImageIO.write(img, "png", output);
            System.out.println("截图已保存至: " + output.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}