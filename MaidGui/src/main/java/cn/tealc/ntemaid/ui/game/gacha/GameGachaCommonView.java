package cn.tealc.ntemaid.ui.game.gacha;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaData;
import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaItem;
import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaPool;
import cn.tealc.ntemaid.model.game.gacha.local.LocalGachaType;
import com.jfoenixN.controls.JFXDialogLayout;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GameGachaCommonView implements FxmlView<GameGachaCommonViewModel>, Initializable {
    @InjectViewModel
    private GameGachaCommonViewModel viewModel;

    @FXML private AnchorPane root;
    @FXML private VBox contentPane;
    @FXML private VBox emptyPane;
    @FXML private Label statusLabel;
    @FXML private HBox playerInfoPane;
    @FXML private ImageView avatarView;
    @FXML private Label roleNameLabel;
    @FXML private Label levelLabel;
    @FXML private Label luckLabel;
    @FXML private HBox poolCardsPane;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private javafx.scene.control.ComboBox<?> accountCombo;
    @FXML private Button refreshBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        accountCombo.setVisible(false);
        accountCombo.setManaged(false);
        refreshBtn.setVisible(false);
        refreshBtn.setManaged(false);

        emptyPane.setVisible(false);
        emptyPane.setManaged(false);

        playerInfoPane.setVisible(false);
        playerInfoPane.setManaged(false);
        poolCardsPane.setVisible(false);
        poolCardsPane.setManaged(false);

        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());

        viewModel.statusMessageProperty().addListener((obs, old, val) -> {
            boolean hasMsg = val != null && !val.isEmpty();
            statusLabel.setVisible(hasMsg);
            statusLabel.setManaged(hasMsg);
            statusLabel.setText(val);
        });

        viewModel.gachaDataProperty().addListener((obs, old, data) -> {
            if (data != null) {
                buildPlayerInfo(data);
                buildPoolCards(data);
                poolCardsPane.setVisible(true);
                poolCardsPane.setManaged(true);
            } else {
                playerInfoPane.setVisible(false);
                playerInfoPane.setManaged(false);
                poolCardsPane.getChildren().clear();
                poolCardsPane.setVisible(false);
                poolCardsPane.setManaged(false);
            }
        });
    }

    @FXML
    void onRefresh(ActionEvent event) {
        // 导入模式下刷新按钮已隐藏
    }

    @FXML
    void importGachaJsonData(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择抽卡数据");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("抽卡数据文件", "*.json"));
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file == null || !file.exists()) {
            return;
        }

        JFXDialogLayout layout = new JFXDialogLayout();
        Label title = new Label("设置游戏ID");
        title.getStyleClass().add(Styles.TITLE_2);
        layout.setHeading(title);

        TextField field = new TextField();
        field.setPromptText("输入玩家ID（可选）");
        Label hint = new Label("请正确输入玩家ID，如果输错了可能会影响后续记录保存");
        hint.getStyleClass().add(Styles.TEXT_SUBTLE);
        VBox body = new VBox(8.0, field, hint);
        layout.setBody(body);

        Button ok = new Button("确认");
        ok.getStyleClass().add(Styles.ACCENT);
        ok.setOnAction(e -> {
            String playerId = field.getText() != null ? field.getText().trim() : "";
            viewModel.importAndAnalyze(file, playerId);
        });
        Button cancel = new Button("取消");
        cancel.setCancelButton(true);
        layout.setActions(ok, cancel);
        NotificationManager.dialog(layout);
    }

    private void buildPlayerInfo(CommonGachaData data) {
        roleNameLabel.setText(data.getPools() != null && !data.getPools().isEmpty()
                ? data.getPools().get(0).getPoolName() : "");
        levelLabel.setText(String.format("共 %d 个卡池",
                data.getPools() != null ? data.getPools().size() : 0));
        luckLabel.setText(analysisLuckName(data.getLuckyType()));
        playerInfoPane.setVisible(true);
        playerInfoPane.setManaged(true);
    }

    private void buildPoolCards(CommonGachaData data) {
        poolCardsPane.getChildren().clear();
        if (data.getPools() == null || data.getPools().isEmpty()) return;
        for (CommonGachaPool pool : data.getPools()) {
            poolCardsPane.getChildren().add(createPoolCard(pool));
        }
    }

    private VBox createPoolCard(CommonGachaPool pool) {
        VBox card = new VBox(5);
        card.getStyleClass().add("pool-card");
        card.setPadding(new Insets(12));
        HBox.setHgrow(card, Priority.ALWAYS);

        // ---- 头部：卡池名 + 运气徽章 + 总抽数 ----
        VBox header = new VBox(2);
        Label poolNameLabel = new Label(pool.getPoolName());
        poolNameLabel.getStyleClass().add("pool-title");
        Label luckBadge = new Label(luckyTypeName(pool.getLuckyType()));
        luckBadge.getStyleClass().addAll("pool-luck", "luck-" + pool.getLuckyType());
        HBox titleRow = new HBox(poolNameLabel, new Spacer(), luckBadge);
        titleRow.setAlignment(Pos.CENTER);

        Label countLabel = new Label(String.valueOf(pool.getTotalCount()));
        countLabel.getStyleClass().add("pool-count");

        Label dateLabel = new Label(pool.getTime() != null ? pool.getTime() : "");
        dateLabel.getStyleClass().add("pool-date");

        header.getChildren().addAll(titleRow, countLabel, dateLabel);

        // ---- 中部：统计信息 ----
        VBox stats = new VBox(5);

        // 五星统计
        stats.getChildren().add(buildStatRow("五星数量", String.format("%d [%.2f%%]",
                pool.getSsrCount(), pct(pool.getSsrCount(), pool.getTotalCount())), "ssr-accent"));
        stats.getChildren().add(buildStatRow("五星平均/最小/最大",
                String.format("%.2f / %d / %d", pool.getSsrAvg(), pool.getSsrMin(), pool.getSsrMax()), "ssr-accent"));

        // 四星统计
        stats.getChildren().add(buildStatRow("四星数量", String.format("%d [%.2f%%]",
                pool.getSrCount(), pct(pool.getSrCount(), pool.getTotalCount())), "sr-accent"));
        stats.getChildren().add(buildStatRow("四星平均/最小/最大",
                String.format("%.2f / %d / %d", pool.getSrAvg(), pool.getSrMin(), pool.getSrMax()), "sr-accent"));

        // 三星统计
        stats.getChildren().add(buildStatRow("三星数量", String.format("%d [%.2f%%]",
                pool.getrCount(), pct(pool.getrCount(), pool.getTotalCount())), "default-accent"));
        stats.getChildren().add(buildStatRow("三星平均/最小/最大",
                String.format("%.2f / %d / %d", pool.getrAvg(), pool.getrMin(), pool.getrMax()), "default-accent"));

        // UP 统计（仅武器池）
        if (pool.getType() == LocalGachaType.WEAPON_POOL) {
            int upTotal = pool.getUpSsrCount() + (int) pool.getNoUpSsrCount();
            stats.getChildren().add(buildStatRow("限定数量", String.format("%d [%.2f%%]",
                    pool.getUpSsrCount(), pct(pool.getUpSsrCount(), upTotal)), "up-accent"));
            stats.getChildren().add(buildStatRow("限定平均", String.format("%.2f",
                    pool.getUpSsrAvg()), "up-accent"));
            stats.getChildren().add(buildStatRow("常驻数量", String.format("%.0f",
                    pool.getNoUpSsrCount()), "default-accent"));
            stats.getChildren().add(buildStatRow("不歪率", String.format("%.2f%%",
                    pool.getNonBannerRate() * 100), "up-accent"));
        }

        // ---- 底部：五星列表 ----
        ListView<CommonGachaItem> listView = new ListView<>();
        listView.getStyleClass().add("gacha-list");
        listView.setItems(FXCollections.observableArrayList(
                pool.getSsrDataList() != null ? pool.getSsrDataList() : List.of()));
        listView.setCellFactory(param -> new SsrItemListCell(pool));
        listView.setPrefHeight(200);
        VBox.setVgrow(listView, Priority.ALWAYS);

        Separator sep = new Separator();
        card.getChildren().addAll(header, sep, stats, listView);
        return card;
    }

    private HBox buildStatRow(String label, String value, String styleClass) {
        Label l = new Label(label);
        l.getStyleClass().add(styleClass);
        Label v = new Label(value);
        v.getStyleClass().add(styleClass);
        HBox row = new HBox(l, new Spacer(), v);
        row.getStyleClass().add("pool-stats");
        return row;
    }

    private static double pct(int part, int total) {
        return total > 0 ? (double) part / total * 100 : 0;
    }

    /** 五星条目单元格 */
    private static class SsrItemListCell extends ListCell<CommonGachaItem> {
        private final BorderPane root;
        private final Label name;
        private final Label date;
        private final Label pity;
        private final ProgressBar progressBar;
        private final CommonGachaPool pool;

        SsrItemListCell(CommonGachaPool pool) {
            this.pool = pool;
            root = new BorderPane();

            name = new Label();
            name.getStyleClass().add("role-name");
            date = new Label();
            date.getStyleClass().add("role-date");
            VBox center = new VBox(name, date);
            center.setPadding(new Insets(0, 0, 0, 10));
            center.setAlignment(Pos.CENTER_LEFT);

            pity = new Label();
            pity.getStyleClass().add("role-name");

            progressBar = new ProgressBar();
            progressBar.setProgress(0);

            root.setCenter(center);
            root.setRight(pity);
            root.setBottom(progressBar);
            root.getStyleClass().addAll("role-cell");
        }

        @Override
        protected void updateItem(CommonGachaItem item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                name.setText(item.getItemName());
                date.setText(item.getTime());
                pity.setText(String.format("%02d", item.getUpCount()));
                progressBar.setProgress((double) item.getUpCount() / pool.getMax());
                setGraphic(root);
            } else {
                setGraphic(null);
            }
        }
    }

    // ---- 运气文本映射 ----

    private static String luckyTypeName(int luckyType) {
        return switch (luckyType) {
            case 1 -> "超非";
            case 2 -> "非";
            case 3 -> "平";
            case 4 -> "欧";
            case 5 -> "超欧";
            default -> "";
        };
    }

    private static String analysisLuckName(int luckyType) {
        return switch (luckyType) {
            case 1 -> "非洲之心";
            case 2 -> "非洲人";
            case 3 -> "平平无奇";
            case 4 -> "欧州人";
            case 5 -> "万里挑一的欧皇";
            default -> "";
        };
    }
}
