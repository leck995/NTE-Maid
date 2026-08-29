package cn.tealc.ntemaid.thread.game.log.event;

import cn.tealc.ntemaid.FXResourcesLoader;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.jna.WindowClientSizeUtil;
import cn.tealc.ntemaid.jna.key.Win32KeySender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
/*
* 大月卡
* */
public class PremiumMonthlyPassEvent implements Consumer<String> {
    private static final Logger log = LoggerFactory.getLogger(PremiumMonthlyPassEvent.class);
    private static final String OPEN_PREMIUM_MONTHLY_PASS = "HideMainform BindFunction by UI UI_CombatAwardSpecialAward Construct";

    private static final String RESOURCE_PATH = "/cn/tealc/ntemaid/data/event/PremiumMonthlyPass.json";
    private static final String EXTERNAL_PATH = "data/event/PremiumMonthlyPass.json";
    private final Win32KeySender win32KeySender;
    private final Map<String, Point2D> screenMap;


    public PremiumMonthlyPassEvent() {
        win32KeySender = new Win32KeySender();
        screenMap = loadScreenMap();
    }

    private static Map<String, Point2D> loadScreenMap() {
        ObjectMapper mapper = new ObjectMapper();
        File externalFile = new File(EXTERNAL_PATH);

        if (externalFile.exists()) {
            // 检查文件是否过期：最后修改时间是否早于 2026-08-30 00:00:00（系统默认时区），暂时存在，三个版本错误文件影响移除后，直接删除
            long lastModified = externalFile.lastModified();
            LocalDate thresholdDate = LocalDate.of(2026, 8, 30);
            Instant threshold = thresholdDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            boolean isExpired = Instant.ofEpochMilli(lastModified).isBefore(threshold);

            if (isExpired) {
                // 过期则删除文件
                boolean deleted = externalFile.delete();
                if (deleted) {
                    log.info("外部坐标文件已过期（修改时间早于 2026-08-30），已删除，将重新生成默认配置");
                } else {
                    log.error("无法删除过期的外部坐标文件，将尝试覆盖写入");
                }
            } else {
                // 未过期，尝试解析
                try {
                    return parseScreenMap(mapper.readTree(externalFile));
                } catch (IOException e) {
                    log.error("读取外部坐标文件失败，将使用默认配置并覆盖", e);
                }
            }
        }
        // 从资源加载默认配置并写入外部文件
        try (InputStream is = FXResourcesLoader.loadStream(RESOURCE_PATH)) {
            if (is != null) {
                JsonNode node = mapper.readTree(is);
                externalFile.getParentFile().mkdirs();
                mapper.writerWithDefaultPrettyPrinter().writeValue(externalFile, node);
                return parseScreenMap(node);
            }
        } catch (IOException e) {
            log.error("读取默认坐标文件失败", e);
        }

        return new HashMap<>();
    }

    private static Map<String, Point2D> parseScreenMap(JsonNode root) {
        Map<String, Point2D> map = new HashMap<>();
        root.fields().forEachRemaining(entry -> {
            JsonNode point = entry.getValue();
            map.put(entry.getKey(), new Point2D(point.get("x").asDouble(), point.get("y").asDouble()));
        });
        return map;
    }

    @Override
    public void accept(String row) {
        monthlyPassSkip(row);
    }

    private void monthlyPassSkip(String row) {
        if (!Config.getSetting().isGameMonthlyPassSkip())
            return;
      if (row.contains(OPEN_PREMIUM_MONTHLY_PASS)){
            win32KeySender.reGetHwnd();
            Point2D size = WindowClientSizeUtil.getSize(win32KeySender.getGameHwnd());
            int width = (int) size.getX();
            int height = (int) size.getY();
            String key = width + "*" + height;
            Point2D point2D = screenMap.get(key);
            if (point2D == null) {
                log.debug("当前窗户尺寸不在预设范围");
                return;
            }
            win32KeySender.clickLeft((int) point2D.getX(), (int) point2D.getY(), Duration.seconds(0.5));
        }
    }
}
