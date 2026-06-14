package cn.tealc.ntemaid.ui.game.gacha;

import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaData;
import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaItem;
import cn.tealc.ntemaid.service.gacha.CommonGachaAnalysisService;
import cn.tealc.ntemaid.service.gacha.CommonGachaService;
import cn.tealc.ntemaid.ui.base.BaseViewModel;
import cn.tealc.ntemaid.util.ImageCacheManager;
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
import java.util.List;

public class GameGachaCommonViewModel extends BaseViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(GameGachaCommonViewModel.class);

    private final CommonGachaAnalysisService analysisService;
    private final CommonGachaService commonGachaService;
    private final ImageCacheManager imageCacheManager;

    private final ObjectProperty<CommonGachaData> gachaData = new SimpleObjectProperty<>();
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);

    @Inject
    public GameGachaCommonViewModel(CommonGachaAnalysisService analysisService,
                                    CommonGachaService commonGachaService,
                                    ImageCacheManager imageCacheManager) {
        this.analysisService = analysisService;
        this.commonGachaService = commonGachaService;
        this.imageCacheManager = imageCacheManager;
    }

    public ImageCacheManager getImageCacheManager() { return imageCacheManager; }

    /**
     * 导入 JSON 文件，持久化到数据库，再读取分析并显示
     */
    public void importAndAnalyze(File file, String playerId) {
        loading.set(true);
        statusMessage.set("");

        Thread.ofVirtual().start(() -> {
            try {
                int saved = commonGachaService.importFromFile(file, playerId);
                List<CommonGachaItem> items = commonGachaService.getByPlayerId(playerId);
                CommonGachaData data = analysisService.analysis(items);
                Platform.runLater(() -> {
                    gachaData.set(data);
                    loading.set(false);
                    statusMessage.set(String.format("导入完成，本次新增 %d 条，共 %d 条记录", saved, items.size()));
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

    // ---- properties ----

    public ObjectProperty<CommonGachaData> gachaDataProperty() { return gachaData; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public BooleanProperty loadingProperty() { return loading; }
}
