package cn.tealc.ntemaid.vision;

import cn.tealc.ntemaid.FXResourcesLoader;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 小地图角色指针检测：颜色过滤 + 路线剔除 + 轮廓方向分析。
 *
 * 流程：模板提取精确 BGR 范围 → inRange → 路线掩膜减法 →
 * 形态学 → findContours → 图像矩方向 → 像素计数消歧。
 */
public class PlayerPointerDetector {
    private static final Logger log = LoggerFactory.getLogger(PlayerPointerDetector.class);

    private static final String POINTER_RESOURCE = "/cn/tealc/ntemaid/image/vision/map/map_player_pointer.png";

    // 轮廓面积范围 (像素)
    private static final double MIN_CONTOUR_AREA = 30.0;
    private static final double MAX_CONTOUR_AREA = 3000.0;

    // 置信度为 1.0 时的参考面积
    private static final double AREA_FOR_FULL_CONFIDENCE = 200.0;

    /** 置信度低于此值则不采用指针识别结果 */
    public static final double CONFIDENCE_THRESHOLD = 0.5;

    // ---- 从模板提取的精确 BGR 范围 (懒加载) ----
    private static volatile Scalar preciseLower = null;
    private static volatile Scalar preciseUpper = null;
    private static final Object rangeLock = new Object();

    /**
     * 从模板 PNG 非透明像素中提取精确 BGR 范围。仅执行一次。
     */
    private static void ensureColorRange() {
        if (preciseLower != null) return;
        synchronized (rangeLock) {
            if (preciseLower != null) return;

            Mat template = loadTemplateFromClasspath();
            if (template == null) {
                // 回退到硬编码范围
                preciseLower = new Scalar(130, 215, 240, 0);
                preciseUpper = new Scalar(200, 255, 255, 0);
                log.warn("无法加载模板，使用默认 BGR 范围");
                return;
            }

            int minB = 255, minG = 255, minR = 255;
            int maxB = 0, maxG = 0, maxR = 0;

            int channels = template.channels();
            byte[] data = new byte[template.rows() * template.cols() * channels];
            template.data().get(data);

            for (int i = 0; i < template.rows() * template.cols(); i++) {
                int b = data[i * channels] & 0xFF;
                int g = data[i * channels + 1] & 0xFF;
                int r = data[i * channels + 2] & 0xFF;
                int a = (channels >= 4) ? (data[i * channels + 3] & 0xFF) : 255;

                if (a > 0) { // 非透明像素
                    if (b < minB) minB = b;
                    if (g < minG) minG = g;
                    if (r < minR) minR = r;
                    if (b > maxB) maxB = b;
                    if (g > maxG) maxG = g;
                    if (r > maxR) maxR = r;
                }
            }

            template.release();

            // ±5 容差用于抗锯齿边缘
            preciseLower = new Scalar(
                    Math.max(0, minB - 5),
                    Math.max(0, minG - 5),
                    Math.max(0, minR - 5), 0);
            preciseUpper = new Scalar(
                    Math.min(255, maxB + 5),
                    Math.min(255, maxG + 5),
                    Math.min(255, maxR + 5), 0);

            log.info("模板 BGR 范围: 模板像素({},{},{})~({},{},{}) → 容差后 Lower=({},{},{}) Upper=({},{},{})",
                    minB, minG, minR, maxB, maxG, maxR,
                    (int) preciseLower.get(0), (int) preciseLower.get(1), (int) preciseLower.get(2),
                    (int) preciseUpper.get(0), (int) preciseUpper.get(1), (int) preciseUpper.get(2));
        }
    }

    private static Mat loadTemplateFromClasspath() {
        InputStream is = PlayerPointerDetector.class.getResourceAsStream(POINTER_RESOURCE);
        if (is == null) {
            is = FXResourcesLoader.loadStream(POINTER_RESOURCE);
        }
        if (is == null) {
            log.error("模板未找到: {}", POINTER_RESOURCE);
            return null;
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            is.close();
            byte[] pngBytes = bos.toByteArray();

            Mat rawBytes = new Mat(1, pngBytes.length, opencv_core.CV_8UC1);
            rawBytes.data().put(pngBytes);
            Mat decoded = opencv_imgcodecs.imdecode(rawBytes, opencv_imgcodecs.IMREAD_UNCHANGED);
            rawBytes.release();
            return decoded;
        } catch (IOException e) {
            log.error("加载模板失败", e);
            return null;
        }
    }

    /**
     * 通过颜色检测小地图中的角色指针。
     */
    public static PointerResult detect(Mat minimapBgr) {
        ensureColorRange();

        // 1. 模板精确 BGR 颜色过滤
        Mat mask = new Mat();
        Mat lowerM = new Mat(minimapBgr.size(), minimapBgr.type(), preciseLower);
        Mat upperM = new Mat(minimapBgr.size(), minimapBgr.type(), preciseUpper);
        opencv_core.inRange(minimapBgr, lowerM, upperM, mask);
        lowerM.release();
        upperM.release();

        int rawPixels = opencv_core.countNonZero(mask);
        log.debug("指针颜色过滤: {} 像素", rawPixels);
        if (rawPixels < MIN_CONTOUR_AREA) {
            mask.release();
            return null;
        }

        // 2. 路线掩膜减法：移除路线黄色像素，避免路线干扰指针形状
        Mat routeMask = RouteDetector.extractRouteMask(minimapBgr);
        // 腐蚀路线掩膜 (保守剔除)
        Mat kernelErode = opencv_imgproc.getStructuringElement(opencv_imgproc.MORPH_RECT, new Size(3, 3));
        opencv_imgproc.erode(routeMask, routeMask, kernelErode);
        kernelErode.release();
        // 从指针 mask 中减去路线
        opencv_core.bitwise_not(routeMask, routeMask);
        opencv_core.bitwise_and(mask, routeMask, mask);
        routeMask.release();

        // 3. 形态学去噪与连接
        Mat kernel3 = opencv_imgproc.getStructuringElement(opencv_imgproc.MORPH_RECT, new Size(3, 3));
        opencv_imgproc.morphologyEx(mask, mask, opencv_imgproc.MORPH_OPEN, kernel3);
        kernel3.release();

        Mat kernel5 = opencv_imgproc.getStructuringElement(opencv_imgproc.MORPH_RECT, new Size(5, 5));
        opencv_imgproc.morphologyEx(mask, mask, opencv_imgproc.MORPH_CLOSE, kernel5);
        kernel5.release();

        // 4. 查找轮廓
        MatVector contours = new MatVector();
        opencv_imgproc.findContours(mask, contours, opencv_imgproc.RETR_EXTERNAL,
                opencv_imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.size() == 0) {
            mask.release();
            contours.close();
            return null;
        }

        // 5. 选取最优轮廓
        double imgCx = minimapBgr.cols() / 2.0;
        double imgCy = minimapBgr.rows() / 2.0;
        int bestIdx = -1;
        double bestScore = -1;
        double bestArea = 0;
        double[] areas = new double[(int) contours.size()];

        for (int i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            double area = opencv_imgproc.contourArea(contour);
            areas[i] = area;

            if (area < MIN_CONTOUR_AREA || area > MAX_CONTOUR_AREA) continue;

            Moments m = opencv_imgproc.moments(contour, false);
            double cx = m.m10() / m.m00();
            double cy = m.m01() / m.m00();

            double distFromCenter = Math.sqrt((cx - imgCx) * (cx - imgCx) + (cy - imgCy) * (cy - imgCy));
            double maxDist = Math.sqrt(imgCx * imgCx + imgCy * imgCy);
            double centerScore = 1.0 - (distFromCenter / maxDist);
            double areaScore = Math.min(1.0, area / AREA_FOR_FULL_CONFIDENCE);
            double score = areaScore * 0.6 + centerScore * 0.4;

            if (score > bestScore) {
                bestScore = score;
                bestIdx = i;
                bestArea = area;
            }
        }

        if (bestIdx < 0) {
            mask.release();
            contours.close();
            return null;
        }

        Mat bestContour = contours.get(bestIdx);
        double area = areas[bestIdx];

        // 6. 图像矩计算主轴方向
        Moments m = opencv_imgproc.moments(bestContour, false);
        double cx = m.m10() / m.m00();
        double cy = m.m01() / m.m00();
        double theta = 0.5 * Math.atan2(2 * m.mu11(), m.mu20() - m.mu02());
        log.debug("矩方向: theta={}°, mu20={}, mu11={}, mu02={}",
                String.format("%.1f", Math.toDegrees(theta)),
                String.format("%.1f", m.mu20()), String.format("%.1f", m.mu11()), String.format("%.1f", m.mu02()));

        // 7. 像素计数消歧 180°
        Rect bbox = opencv_imgproc.boundingRect(bestContour);
        double ax = Math.cos(theta);
        double ay = Math.sin(theta);

        byte[] maskData = new byte[mask.rows() * mask.cols()];
        mask.data().get(maskData);

        int frontCount = 0, backCount = 0;
        int endX = Math.min(bbox.x() + bbox.width(), mask.cols());
        int endY = Math.min(bbox.y() + bbox.height(), mask.rows());
        for (int py = Math.max(bbox.y(), 0); py < endY; py++) {
            for (int px = Math.max(bbox.x(), 0); px < endX; px++) {
                if (maskData[py * mask.cols() + px] != 0) {
                    double proj = (px - cx) * ax + (py - cy) * ay;
                    if (proj > 0) frontCount++;
                    else backCount++;
                }
            }
        }

        double alpha = frontCount > backCount ? theta : theta + Math.PI;
        log.debug("消歧: bbox=({},{},{},{}), 像素(正半轴={}, 负半轴={}), alpha={}°",
                bbox.x(), bbox.y(), bbox.width(), bbox.height(),
                frontCount, backCount,
                String.format("%.1f", Math.toDegrees(alpha)));

        // 8. 转为"以正上为 0°、顺时针为正"
        double arrowAngleRad = alpha + Math.PI / 2;
        if (arrowAngleRad > Math.PI) arrowAngleRad -= 2 * Math.PI;
        if (arrowAngleRad < -Math.PI) arrowAngleRad += 2 * Math.PI;
        double angleDeg = Math.toDegrees(arrowAngleRad);

        double confidence = Math.min(1.0, area / AREA_FOR_FULL_CONFIDENCE);
        int ix = (int) Math.round(cx);
        int iy = (int) Math.round(cy);

        // 诊断输出
        opencv_imgcodecs.imwrite("pointer_mask.png", mask);
        drawDebugOverlay(minimapBgr, bestContour, cx, cy, alpha, bbox);

        mask.release();
        contours.close();

        log.debug("检测到角色指针: 角度={}, 置信度={}, 面积={}px, 坐标=({},{})",
                String.format("%.1f", angleDeg), String.format("%.3f", confidence),
                (int) area, ix, iy);
        return new PointerResult(angleDeg, confidence, ix, iy);
    }

    private static void drawDebugOverlay(Mat bgr, Mat contour, double cx, double cy,
                                         double alpha, Rect bbox) {
        Mat debug = bgr.clone();
        Scalar red = new Scalar(0, 0, 255, 0);
        Scalar green = new Scalar(0, 255, 0, 0);
        Scalar blue = new Scalar(255, 0, 0, 0);

        opencv_imgproc.rectangle(debug, bbox, green, 1, 0, 0);
        opencv_imgproc.circle(debug, new Point((int) Math.round(cx), (int) Math.round(cy)),
                3, red, -1, 0, 0);

        double len = 40;
        int x2 = (int) Math.round(cx + len * Math.cos(alpha));
        int y2 = (int) Math.round(cy + len * Math.sin(alpha));
        opencv_imgproc.line(debug,
                new Point((int) Math.round(cx), (int) Math.round(cy)),
                new Point(x2, y2), blue, 2, 0, 0);

        opencv_imgcodecs.imwrite("pointer_debug.png", debug);
        debug.release();
    }

    public static RouteResult.Turn classifyTurnFromPointer(double angle, double confidence) {
        if (confidence < CONFIDENCE_THRESHOLD) return RouteResult.Turn.NO_ROUTE;
        double a = Math.abs(angle);
        if (a < 15.0) return RouteResult.Turn.STRAIGHT;
        if (a < 30.0) return angle > 0 ? RouteResult.Turn.SLIGHT_RIGHT : RouteResult.Turn.SLIGHT_LEFT;
        if (a < 60.0) return angle > 0 ? RouteResult.Turn.RIGHT        : RouteResult.Turn.LEFT;
        if (a < 90.0) return angle > 0 ? RouteResult.Turn.HARD_RIGHT    : RouteResult.Turn.HARD_LEFT;
        return RouteResult.Turn.UTURN;
    }

    public static void dispose() {}
}
