package cn.tealc.ntemaid.ui.game.gacha;

import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaData;
import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaItem;
import cn.tealc.ntemaid.service.ConfigService;
import cn.tealc.ntemaid.service.gacha.CommonGachaAnalysisService;
import cn.tealc.ntemaid.service.gacha.CommonGachaService;
import cn.tealc.ntemaid.ui.base.BaseViewModel;
import cn.tealc.ntemaid.util.ImageCacheManager;
import cn.tealc.teafx.utils.message.MessageInfo;
import com.google.inject.Inject;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.utils.notifications.NotificationObserver;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class GameGachaCommonViewModel extends BaseViewModel implements SceneLifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(GameGachaCommonViewModel.class);
    private static final String GACHA_COMMON_SELECTED = "GACHA_COMMON_SELECTED";

    private final CommonGachaAnalysisService analysisService;
    private final CommonGachaService commonGachaService;
    private final ConfigService configService;
    private final ImageCacheManager imageCacheManager;

    private final ObservableList<String> playerIds = FXCollections.observableArrayList();
    private final ObjectProperty<CommonGachaData> gachaData = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty hasData = new SimpleBooleanProperty(false);
    private final StringProperty selectedPlayerId = new SimpleStringProperty("");

    /** 监听 GachaControlDialog 抓取完成事件，接收结果文件并导入分析 */
    private final NotificationObserver captureFinishedObserver = (key, objects) -> {
        String pid = (String) objects[0];
        File file =(File) objects[1];
        if (file == null || !file.exists() || pid == null || pid.isEmpty()) {
            loading.set(false);
            NotificationManager.message(MessageInfo.error("抓取失败：未获取到抽卡数据"));
            return;
        }
        selectedPlayerId.set(pid);
        importAndAnalyze(file, pid);
    };

    @Inject
    public GameGachaCommonViewModel(CommonGachaAnalysisService analysisService,
                                    CommonGachaService commonGachaService,
                                    ConfigService configService,
                                    ImageCacheManager imageCacheManager) {
        this.analysisService = analysisService;
        this.commonGachaService = commonGachaService;
        this.configService = configService;
        this.imageCacheManager = imageCacheManager;

    }

    public ImageCacheManager getImageCacheManager() { return imageCacheManager; }

    private void loadPlayerIdsAsync(Runnable onDone) {
        Thread.ofVirtual().start(() -> {
            List<String> ids = commonGachaService.getDistinctPlayerIds();
            Platform.runLater(() -> {
                playerIds.setAll(ids);
                hasData.set(!playerIds.isEmpty());
                if (onDone != null) onDone.run();
            });
        });
    }

    /**
     * 界面首次加载时调用，异步加载 playerId 列表并恢复上次选中
     */
    public void initPlayerIds() {
        loadPlayerIdsAsync(() -> {
            if (!playerIds.isEmpty()) {
                String saved = configService.getConfig(GACHA_COMMON_SELECTED).orElse("");
                if (!saved.isEmpty() && playerIds.contains(saved)) {
                    selectPlayer(saved);
                } else {
                    selectPlayer(playerIds.get(0));
                }
            }
        });
    }

    public void selectPlayer(String playerId) {
        if (playerId == null || playerId.isEmpty()) {
            gachaData.set(null);
            return;
        }
        selectedPlayerId.set(playerId);
        configService.setConfig(GACHA_COMMON_SELECTED, playerId);
        loading.set(true);

        Thread.ofVirtual().start(() -> {
            try {
                List<CommonGachaItem> items = commonGachaService.getByPlayerId(playerId);
                CommonGachaData data = analysisService.analysis(items);
                Platform.runLater(() -> {
                    gachaData.set(data);
                    loading.set(false);
                });
            } catch (Exception e) {
                LOG.error("加载抽卡数据失败, playerId={}", playerId, e);
                Platform.runLater(() -> {
                    loading.set(false);
                    NotificationManager.message(MessageInfo.error("加载失败: " + e.getMessage()));
                });
            }
        });
    }

    public void importAndAnalyze(File file, String playerId) {
        loading.set(true);

        Thread.ofVirtual().start(() -> {
            try {
                int added = commonGachaService.importFromFile(file, playerId);
                List<CommonGachaItem> items = commonGachaService.getByPlayerId(playerId);
                CommonGachaData data = analysisService.analysis(items);
                Platform.runLater(() -> loadPlayerIdsAsync(() -> {
                    selectedPlayerId.set(playerId);
                    configService.setConfig(GACHA_COMMON_SELECTED, playerId);
                    gachaData.set(data);
                    loading.set(false);
                    NotificationManager.message(
                            MessageInfo.success(String.format("导入完成，新增 %d 条，共 %d 条记录", added, items.size())));
                }));
            } catch (Exception e) {
                LOG.error("导入抽卡数据失败", e);
                Platform.runLater(() -> {
                    loading.set(false);
                    NotificationManager.message(MessageInfo.error("导入失败: " + e.getMessage()));
                });
            }
        });
    }

    public void deleteCurrentPlayer(String playerId) {
        if (playerId == null || playerId.isEmpty()) return;

        Thread.ofVirtual().start(() -> {
            try {
                commonGachaService.deleteByPlayerId(playerId);
                Platform.runLater(() -> {
                    gachaData.set(null);
                    loadPlayerIdsAsync(() -> {
                        if (!playerIds.isEmpty()) {
                            selectedPlayerId.set(playerIds.get(0));
                            selectPlayer(playerIds.get(0));
                        }
                        NotificationManager.message(MessageInfo.info("已删除玩家 " + playerId + " 的数据"));
                    });
                });
            } catch (Exception e) {
                LOG.error("删除抽卡数据失败, playerId={}", playerId, e);
                Platform.runLater(() ->
                        NotificationManager.message(MessageInfo.error("删除失败: " + e.getMessage())));
            }
        });
    }

    @Override
    public void onViewAdded() {
        NotificationManager.subscribe(NotificationKey.GACHA_CAPTURE_FINISHED, captureFinishedObserver);
    }

    public void onViewRemoved() {
        NotificationManager.unsubscribe(NotificationKey.GACHA_CAPTURE_FINISHED, captureFinishedObserver);
    }

    // ---- properties ----

    public ObservableList<String> playerIdsProperty() { return playerIds; }
    public ObjectProperty<CommonGachaData> gachaDataProperty() { return gachaData; }
    public BooleanProperty loadingProperty() { return loading; }
    public BooleanProperty hasDataProperty() { return hasData; }
    public StringProperty selectedPlayerIdProperty() { return selectedPlayerId; }

    public String getSelectedPlayerId() {
        return selectedPlayerId.get();
    }
}