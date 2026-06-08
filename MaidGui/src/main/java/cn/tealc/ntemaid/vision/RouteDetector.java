package cn.tealc.ntemaid.vision;

import cn.tealc.ntemaid.snapshot.WindowCapture;
import com.sun.jna.platform.win32.WinDef.HWND;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.Vec4iVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 小地图路线识别：截取小地图 → 颜色过滤 → HoughLinesP 线段检测 → 方向分析
 */
public class RouteDetector {
    private static final Logger log = LoggerFactory.getLogger(RouteDetector.class);

    // 黄色 HSV 范围 (需根据实际截图标定)
    private static final Scalar LOWER_YELLOW = new Scalar(20, 80, 80, 0);
    private static final Scalar UPPER_YELLOW = new Scalar(35, 255, 255, 0);
    // 蓝色 HSV 范围 主色 #0FD4FF，加宽容差覆盖深浅变化
    private static final Scalar LOWER_BLUE = new Scalar(80, 50, 50, 0);
    private static final Scalar UPPER_BLUE = new Scalar(145, 255, 255, 0);

    // 转弯判定角度阈值
    private static final double ANGLE_STRAIGHT = 30.0;
    private static final double ANGLE_SLIGHT = 45.0;
    private static final double ANGLE_TURN = 70.0;
    private static final double ANGLE_HARD = 100.0;
    private static final double ANGLE_UTURN = 150.0;

    private final HWND hwnd;
    private final Rectangle minimapRoi;

    public RouteDetector(HWND hwnd, Rectangle minimapRoi) {
        this.hwnd = hwnd;
        this.minimapRoi = minimapRoi;
    }

    /**
     * 截图并分析当前路线
     */
    public RouteResult analyze() {
        BufferedImage screenshot = WindowCapture.captureGameClientArea(hwnd);
        BufferedImage minimap = screenshot.getSubimage(
                minimapRoi.x, minimapRoi.y, minimapRoi.width, minimapRoi.height);
        try {
            ImageIO.write(screenshot,"png",new File("screenshot.png"));
            ImageIO.write(minimap,"png",new File("a.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return analyze(minimap);
    }

    /**
     * 直接分析小地图图像
     */
    public static RouteResult analyze(BufferedImage minimapImage) {
        Mat bgr = bufferedImageToMat(minimapImage);
        Mat mask = extractRouteMask(bgr);

        List<LineSeg> segments = detectLineSegments(mask);

        // 保存路线 mask
        opencv_imgcodecs.imwrite("route_mask.png", mask);
        mask.release();

        double mapCx = minimapImage.getWidth() / 2.0;
        double mapCy = minimapImage.getHeight() / 2.0;

        // 过滤：只保留很靠近地图中心的线段
        double maxDist = Math.min(minimapImage.getWidth(), minimapImage.getHeight()) * 0.3;
        List<LineSeg> nearCenter = filterNearCenter(segments, mapCx, mapCy, maxDist);
        log.debug("线段过滤: {}/{} 条靠近中心 (maxDist={})", nearCenter.size(), segments.size(),
                String.format("%.0f", maxDist));

        if (nearCenter.isEmpty()) {
            bgr.release();
            return new RouteResult(RouteResult.Turn.NO_ROUTE, 0, 0);
        }

        List<LineSeg> ordered = chainSegments(nearCenter);

        // 诊断：绘制所有线段和排序后线段
        drawRouteDebug(bgr, segments, ordered, mapCx, mapCy);
        bgr.release();

        return analyzeDirection(ordered, mapCx, mapCy);
    }

    /** 在小地图上绘制路线线段，保存为 route_debug.png */
    private static void drawRouteDebug(Mat bgr, List<LineSeg> allSegments,
                                        List<LineSeg> ordered, double mapCx, double mapCy) {
        Mat debug = bgr.clone();
        Scalar green = new Scalar(0, 255, 0, 0);   // 排序后线段
        Scalar gray = new Scalar(128, 128, 128, 0); // 未排序线段
        Scalar red = new Scalar(0, 0, 255, 0);      // 中心点

        // 绘制所有 HoughLinesP 检测到的线段 (灰色)
        for (LineSeg seg : allSegments) {
            opencv_imgproc.line(debug,
                    new org.bytedeco.opencv.opencv_core.Point(seg.x1, seg.y1),
                    new org.bytedeco.opencv.opencv_core.Point(seg.x2, seg.y2),
                    gray, 1, 0, 0);
        }

        // 绘制排序链接后的线段 (绿色，略粗)
        for (LineSeg seg : ordered) {
            opencv_imgproc.line(debug,
                    new org.bytedeco.opencv.opencv_core.Point(seg.x1, seg.y1),
                    new org.bytedeco.opencv.opencv_core.Point(seg.x2, seg.y2),
                    green, 2, 0, 0);
        }

        // 标记地图中心 (红色圆)
        opencv_imgproc.circle(debug,
                new org.bytedeco.opencv.opencv_core.Point(
                        (int) Math.round(mapCx), (int) Math.round(mapCy)),
                4, red, 1, 0, 0);

        opencv_imgcodecs.imwrite("route_debug.png", debug);
        debug.release();
    }

    /**
     * 颜色阈值过滤，提取路线二值蒙版
     */
    static Mat extractRouteMask(Mat bgr) {
        Mat hsv = new Mat();
        opencv_imgproc.cvtColor(bgr, hsv, opencv_imgproc.COLOR_BGR2HSV);

        // 创建与 hsv 同尺寸的 Scalar Mat
        Mat lowerY = new Mat(hsv.size(), hsv.type(), LOWER_YELLOW);
        Mat upperY = new Mat(hsv.size(), hsv.type(), UPPER_YELLOW);
        Mat lowerB = new Mat(hsv.size(), hsv.type(), LOWER_BLUE);
        Mat upperB = new Mat(hsv.size(), hsv.type(), UPPER_BLUE);

        Mat yellowMask = new Mat();
        Mat blueMask = new Mat();
        opencv_core.inRange(hsv, lowerY, upperY, yellowMask);
        opencv_core.inRange(hsv, lowerB, upperB, blueMask);

        hsv.release();
        lowerY.release();
        upperY.release();
        lowerB.release();
        upperB.release();

        Mat routeMask = new Mat();
        opencv_core.bitwise_or(yellowMask, blueMask, routeMask);
        yellowMask.release();
        blueMask.release();

        // 形态学闭运算：连接断裂的线段 (5x5 核，弥补蓝色变色造成的断裂)
        Mat kernel = opencv_imgproc.getStructuringElement(
                opencv_imgproc.MORPH_RECT, new Size(5, 5));
        opencv_imgproc.morphologyEx(routeMask, routeMask, opencv_imgproc.MORPH_CLOSE, kernel);
        kernel.release();

        log.debug("route mask non-zero pixels: {}", opencv_core.countNonZero(routeMask));
        return routeMask;
    }

    /**
     * HoughLinesP 检测线段
     */
    private static List<LineSeg> detectLineSegments(Mat mask) {
        Vec4iVector lines = new Vec4iVector();
        opencv_imgproc.HoughLinesP(mask, lines, 1, Math.PI / 180, 15, 10, 10);

        List<LineSeg> segments = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            int[] pts = new int[4];
            lines.get(i).get(pts);
            segments.add(new LineSeg(pts[0], pts[1], pts[2], pts[3]));
        }
        lines.releaseReference();

        log.debug("HoughLinesP detected {} segments", segments.size());
        return segments;
    }

    /**
     * 过滤线段：只保留至少一个端点在距中心 maxDist 范围内的
     */
    private static List<LineSeg> filterNearCenter(List<LineSeg> segments,
                                                   double mapCx, double mapCy, double maxDist) {
        double maxDistSq = maxDist * maxDist;
        List<LineSeg> result = new ArrayList<>();
        for (LineSeg seg : segments) {
            double d1 = (seg.x1 - mapCx) * (seg.x1 - mapCx) + (seg.y1 - mapCy) * (seg.y1 - mapCy);
            double d2 = (seg.x2 - mapCx) * (seg.x2 - mapCx) + (seg.y2 - mapCy) * (seg.y2 - mapCy);
            if (d1 <= maxDistSq || d2 <= maxDistSq) {
                result.add(seg);
            }
        }
        return result;
    }

    /**
     * 按端点距离排序，将无序的线段连成有向路径
     */
    private static List<LineSeg> chainSegments(List<LineSeg> input) {
        if (input.size() <= 1) return new ArrayList<>(input);

        List<LineSeg> remaining = new ArrayList<>(input);
        List<LineSeg> ordered = new ArrayList<>();

        ordered.add(remaining.remove(0));

        while (!remaining.isEmpty()) {
            LineSeg last = ordered.get(ordered.size() - 1);
            int bestIdx = 0;
            double bestDist = Double.MAX_VALUE;
            for (int i = 0; i < remaining.size(); i++) {
                LineSeg seg = remaining.get(i);
                double d = Math.min(last.endDistTo(seg), last.endDistTo(seg.reversed()));
                if (d < bestDist) {
                    bestDist = d;
                    bestIdx = i;
                }
            }

            LineSeg next = remaining.remove(bestIdx);
            if (last.endDistTo(next) > last.endDistTo(next.reversed())) {
                next = next.reversed();
            }
            ordered.add(next);
        }

        return ordered;
    }

    /**
     * 分析线段链的方向变化，输出转弯判定。
     * 仅使用靠近中心的局部线段，比较前半段和后半段的方向差。
     */
    private static RouteResult analyzeDirection(List<LineSeg> ordered, double mapCx, double mapCy) {
        if (ordered.size() < 3) {
            return new RouteResult(RouteResult.Turn.STRAIGHT, 0,
                    Math.min(1.0, ordered.size() / 5.0));
        }

        // 将链条分成前后两半，分别计算总体方向
        int mid = ordered.size() / 2;
        double frontSumX = 0, frontSumY = 0;
        double backSumX = 0, backSumY = 0;

        for (int i = 0; i < mid; i++) {
            frontSumX += ordered.get(i).dx();
            frontSumY += ordered.get(i).dy();
        }
        for (int i = mid; i < ordered.size(); i++) {
            backSumX += ordered.get(i).dx();
            backSumY += ordered.get(i).dy();
        }

        double frontAngle = Math.toDegrees(Math.atan2(frontSumY, frontSumX));
        double backAngle = Math.toDegrees(Math.atan2(backSumY, backSumX));
        double angleDiff = angleDiff(frontAngle, backAngle);

        log.debug("direction: 前半段={}°, 后半段={}°, 角度差={}°",
                String.format("%.1f", frontAngle), String.format("%.1f", backAngle),
                String.format("%.1f", angleDiff));

        // 直行：角度差小于阈值
        if (Math.abs(angleDiff) < ANGLE_STRAIGHT) {
            log.debug("角度差小，判定直行");
            return new RouteResult(RouteResult.Turn.STRAIGHT, angleDiff,
                    computeConfidence(ordered, RouteResult.Turn.STRAIGHT));
        }

        RouteResult.Turn turn = classifyByPosition(ordered, angleDiff, mapCx, mapCy);
        double confidence = computeConfidence(ordered, turn);

        log.debug("direction analysis: angleDiff={}, turn={}, confidence={}",
                String.format("%.1f", angleDiff), turn, String.format("%.2f", confidence));
        return new RouteResult(turn, angleDiff, confidence);
    }

    private static RouteResult.Turn classifyTurn(double signedAngle) {
        double a = Math.abs(signedAngle);
        if (a < ANGLE_STRAIGHT) return RouteResult.Turn.STRAIGHT;
        if (a < ANGLE_SLIGHT)  return signedAngle > 0 ? RouteResult.Turn.SLIGHT_LEFT  : RouteResult.Turn.SLIGHT_RIGHT;
        if (a < ANGLE_TURN)    return signedAngle > 0 ? RouteResult.Turn.LEFT         : RouteResult.Turn.RIGHT;
        if (a < ANGLE_HARD)    return signedAngle > 0 ? RouteResult.Turn.HARD_LEFT     : RouteResult.Turn.HARD_RIGHT;
        return RouteResult.Turn.UTURN;
    }

    /**
     * 根据路线相对地图中心的位置 + 弯曲方向判定转弯方向。
     * 地图方向固定，角色始终位于地图正中间。
     *
     * @param ordered      排序后的线段链
     * @param maxAngleDiff 滑动窗口计算的最大方向变化角 (正=逆时针/左, 负=顺时针/右)
     * @param mapCx        地图中心 X
     * @param mapCy        地图中心 Y
     */
    private static RouteResult.Turn classifyByPosition(List<LineSeg> ordered, double maxAngleDiff,
                                                       double mapCx, double mapCy) {
        // 计算路线质心
        double sumX = 0, sumY = 0;
        for (LineSeg seg : ordered) {
            sumX += seg.midX();
            sumY += seg.midY();
        }
        double routeCx = sumX / ordered.size();
        double routeCy = sumY / ordered.size();

        double dx = routeCx - mapCx;
        double dy = routeCy - mapCy;

        boolean curvePos = maxAngleDiff > 0; // true = 逆时针弯曲

        log.debug("路线位置: route中心=({},{}), map中心=({},{}), 偏移dx={}, dy={}, maxAngleDiff={}",
                String.format("%.0f", routeCx), String.format("%.0f", routeCy),
                String.format("%.0f", mapCx), String.format("%.0f", mapCy),
                String.format("%.1f", dx), String.format("%.1f", dy),
                String.format("%.1f", maxAngleDiff));

        // 判断主方向 (左/右 vs 上/下)
        if (Math.abs(dx) > Math.abs(dy)) {
            // 路线偏左或偏右
            if (dx < 0) {
                // 路线在中心左侧
                if (curvePos) {
                    // 逆时针弯曲 = 向下 → 左转
                    log.debug("路线偏左 + 弯曲向下 → 左转");
                    return RouteResult.Turn.LEFT;
                } else {
                    // 顺时针弯曲 = 向上 → 右转
                    log.debug("路线偏左 + 弯曲向上 → 右转");
                    return RouteResult.Turn.RIGHT;
                }
            } else {
                // 路线在中心右侧: maxAngleDiff>0=向上弯 → 左转, <0=向下弯 → 右转
                if (curvePos) {
                    log.debug("路线偏右 + 弯曲向上 → 左转");
                    return RouteResult.Turn.LEFT;
                } else {
                    log.debug("路线偏右 + 弯曲向下 → 右转");
                    return RouteResult.Turn.RIGHT;
                }
            }
        } else {
            // 路线偏上或偏下
            if (dy < 0) {
                // 路线在中心上方: maxAngleDiff>0=向右弯 → 右转, <0=向左弯 → 左转
                if (curvePos) {
                    log.debug("路线偏上 + 弯曲向右 → 右转");
                    return RouteResult.Turn.RIGHT;
                } else {
                    log.debug("路线偏上 + 弯曲向左 → 左转");
                    return RouteResult.Turn.LEFT;
                }
            } else {
                // 路线在中心下方
                if (curvePos) {
                    // 逆时针弯曲 = 向左 → 右转
                    log.debug("路线偏下 + 弯曲向左 → 右转");
                    return RouteResult.Turn.RIGHT;
                } else {
                    // 顺时针弯曲 = 向右 → 左转
                    log.debug("路线偏下 + 弯曲向右 → 左转");
                    return RouteResult.Turn.LEFT;
                }
            }
        }
    }

    private static double computeConfidence(List<LineSeg> ordered, RouteResult.Turn turn) {
        if (turn == RouteResult.Turn.NO_ROUTE) return 0;
        double avgLength = ordered.stream().mapToDouble(LineSeg::length).average().orElse(0);
        return Math.min(1.0, ordered.size() / 5.0) * Math.min(1.0, avgLength / 20.0);
    }

    private static double angleDiff(double a1, double a2) {
        double diff = a2 - a1;
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        return diff;
    }

    /**
     * BufferedImage (TYPE_INT_RGB) → OpenCV Mat (BGR)
     */
    private static Mat bufferedImageToMat(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        Mat mat = new Mat(height, width, opencv_core.CV_8UC3);
        byte[] data = new byte[width * height * 3];
        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            data[i * 3] = (byte) (p & 0xFF);
            data[i * 3 + 1] = (byte) ((p >> 8) & 0xFF);
            data[i * 3 + 2] = (byte) ((p >> 16) & 0xFF);
        }
        mat.data().put(data);
        return mat;
    }

    // ---- 线段数据对象 ----

    private static class LineSeg {
        final int x1, y1, x2, y2;

        LineSeg(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        double dx() { return x2 - x1; }
        double dy() { return y2 - y1; }
        double midX() { return (x1 + x2) / 2.0; }
        double midY() { return (y1 + y2) / 2.0; }

        double length() { return Math.sqrt(dx() * dx() + dy() * dy()); }

        double endDistTo(LineSeg other) {
            double dx = x2 - other.x1;
            double dy = y2 - other.y1;
            return Math.sqrt(dx * dx + dy * dy);
        }

        LineSeg reversed() { return new LineSeg(x2, y2, x1, y1); }
    }
}
