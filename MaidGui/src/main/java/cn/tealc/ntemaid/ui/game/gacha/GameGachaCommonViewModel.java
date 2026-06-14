package cn.tealc.ntemaid.ui.game.gacha;

import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaData;
import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaItem;
import cn.tealc.ntemaid.service.gacha.CommonGachaAnalysisService;
import cn.tealc.ntemaid.ui.base.BaseViewModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GameGachaCommonViewModel extends BaseViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(GameGachaCommonViewModel.class);

    private final CommonGachaAnalysisService analysisService;
    private final ObjectMapper mapper;

    private final ObjectProperty<CommonGachaData> gachaData = new SimpleObjectProperty<>();
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);

    @Inject
    public GameGachaCommonViewModel(CommonGachaAnalysisService analysisService,
                                    ObjectMapper mapper) {
        this.analysisService = analysisService;
        this.mapper = mapper;
    }

    /**
     * 导入 JSON 文件并执行抽卡分析，结果写入 gachaData
     */
    public void importAndAnalyze(File file, String playerId) {
        loading.set(true);
        statusMessage.set("");

        Thread.ofVirtual().start(() -> {
            try {
                List<CommonGachaItem> items = parseGachaFile(file, playerId);
                CommonGachaData data = analysisService.analysis(items);
                Platform.runLater(() -> {
                    gachaData.set(data);
                    loading.set(false);
                    statusMessage.set(String.format("导入完成，共 %d 条记录", items.size()));
                });
            } catch (Exception e) {
                LOG.error("导入抽卡数据失败", e);
                Platform.runLater(() -> {
                    loading.set(false);
                    statusMessage.set("导入失败: " + e.getMessage());
                });
            }
        });
    }

    /**
     * 解析 JSON 文件，读取 nte.list 节点
     */
    private List<CommonGachaItem> parseGachaFile(File file, String playerId) throws IOException {
        JsonNode root = mapper.readTree(file);
        JsonNode listNode = root.path("nte").path("list");
        if (listNode.isMissingNode() || !listNode.isArray()) {
            throw new IOException("无效的JSON文件：缺少 nte.list 数组");
        }

        List<CommonGachaItem> items = mapper.readerFor(new TypeReference<List<CommonGachaItem>>() {})
                .readValue(listNode);

        if (playerId != null && !playerId.isEmpty()) {
            for (CommonGachaItem item : items) {
                item.setPlayerId(playerId);
            }
        }

        return items;
    }

    // ---- properties ----

    public ObjectProperty<CommonGachaData> gachaDataProperty() { return gachaData; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public BooleanProperty loadingProperty() { return loading; }
}
