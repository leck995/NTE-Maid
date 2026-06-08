package cn.tealc.ntemaid.vision;

/**
 * 角色指针检测结果。
 */
public class PointerResult {
    /** 旋转角度（度）。0 = 正上，正 = 顺时针（右转），负 = 逆时针（左转） */
    private final double angle;

    /** 匹配置信度 (0.0–1.0) */
    private final double confidence;

    /** 小地图上匹配位置的 X 坐标 */
    private final int x;

    /** 小地图上匹配位置的 Y 坐标 */
    private final int y;

    public PointerResult(double angle, double confidence, int x, int y) {
        this.angle = angle;
        this.confidence = confidence;
        this.x = x;
        this.y = y;
    }

    public double getAngle() { return angle; }
    public double getConfidence() { return confidence; }
    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public String toString() {
        return String.format("Pointer(angle=%.1f°, conf=%.3f, pos=(%d,%d))",
                angle, confidence, x, y);
    }
}
