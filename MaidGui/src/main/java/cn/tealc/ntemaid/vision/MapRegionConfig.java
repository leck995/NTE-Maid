package cn.tealc.ntemaid.vision;

import java.awt.*;

/**
 * 小地图区域配置，按分辨率映射 ROI 矩形
 */
public class MapRegionConfig {

    private static final java.util.Map<String, Rectangle> regionMap = new java.util.HashMap<>();

    static {
        // 各分辨率下小地图在窗口客户区中的位置和大小 (x, y, width, height)
        // TODO: 根据实际截图标定具体数值
        regionMap.put("2560*1440", new Rectangle(2240, 20, 300, 300));
        regionMap.put("1920*1080", new Rectangle(1620, 20, 280, 280));
        regionMap.put("2560*1080", new Rectangle(2280, 20, 280, 280));
        regionMap.put("1600*900",  new Rectangle(1320, 15, 260, 260));
        regionMap.put("1280*720",  new Rectangle(45, 40, 130, 125));
    }

    private MapRegionConfig() {}

    /**
     * @param width  窗口客户区宽度
     * @param height 窗口客户区高度
     * @return 小地图区域，未配置时返回 null
     */
    public static Rectangle getMinimapRegion(int width, int height) {
        String key = width + "*" + height;
        return regionMap.get(key);
    }

    /**
     * 手动注册一个分辨率的 ROI
     */
    public static void register(int width, int height, Rectangle roi) {
        regionMap.put(width + "*" + height, roi);
    }
}
