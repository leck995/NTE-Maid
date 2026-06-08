package cn.tealc.ntemaid.vision;

/**
 * 小地图路线识别结果
 */
public class RouteResult {

    public enum Turn {
        STRAIGHT,       // 直行
        SLIGHT_LEFT,    // 微左转
        LEFT,           // 左转
        HARD_LEFT,      // 急左转
        SLIGHT_RIGHT,   // 微右转
        RIGHT,          // 右转
        HARD_RIGHT,     // 急右转
        UTURN,          // 掉头
        NO_ROUTE        // 未检测到路线
    }

    private final Turn turn;
    private final double angle;
    private final double confidence;

    public RouteResult(Turn turn, double angle, double confidence) {
        this.turn = turn;
        this.angle = angle;
        this.confidence = confidence;
    }

    public Turn getTurn() {
        return turn;
    }

    public double getAngle() {
        return angle;
    }

    public double getConfidence() {
        return confidence;
    }

    public boolean hasRoute() {
        return turn != Turn.NO_ROUTE;
    }

    public boolean isTurning() {
        return turn != Turn.STRAIGHT && turn != Turn.NO_ROUTE;
    }

    @Override
    public String toString() {
        if (turn == Turn.NO_ROUTE) {
            return "无路线";
        }
        return String.format("%s (%.1f°, 置信度: %.2f)", turn, angle, confidence);
    }
}
