package cn.tealc.ntemaid.thread.game.log.event;

import cn.tealc.ntemaid.FXResourcesLoader;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.jna.key.Win32KeySender;
import cn.tealc.ntemaid.jna.WindowClientSizeUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class OtherEvent implements Consumer<String> {
    private static final Logger log = LoggerFactory.getLogger(OtherEvent.class);
    private static final String OPEN_ADVENTURE_MANUAL = "OnUIOpened ResetbIgnoreInputing UIName:AdventureManual";
    private static final String OPEN_ADVENTURE_MANUAL_FROM_MONSTER = "LogHTMonsterManual: Warning: IsMonsterAllKilled, Guid";
    private static final String RESOURCE_PATH = "/cn/tealc/ntemaid/data/event/AdventureManual.json";
    private static final String EXTERNAL_PATH = "data/event/AdventureManual.json";

    private final Win32KeySender win32KeySender;
    private final Map<String, Point2D> screenMap;

    private boolean skip = false;

    public OtherEvent() {
        win32KeySender = new Win32KeySender();
        screenMap = loadScreenMap();
    }

    private static Map<String, Point2D> loadScreenMap() {
        ObjectMapper mapper = new ObjectMapper();
        File externalFile = new File(EXTERNAL_PATH);

        if (externalFile.exists()) {
            try {
                return parseScreenMap(mapper.readTree(externalFile));
            } catch (IOException e) {
                log.error("读取外部坐标文件失败，将使用默认配置", e);
            }
        }

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
        adventureManualSkip(row);
    }

    private void adventureManualSkip(String row) {
        if (!Config.getSetting().isAdventureManualSkip())
            return;
        if (row.contains(OPEN_ADVENTURE_MANUAL_FROM_MONSTER)){
            skip = true;
        } else if (row.contains(OPEN_ADVENTURE_MANUAL)){
            if (skip){
                skip = false;
                return;
            }
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
