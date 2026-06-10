package cn.tealc.ntemaid.ui.game.gacha;

import atlantafx.base.controls.Spacer;
import cn.tealc.ntemaid.model.game.Character;
import cn.tealc.ntemaid.model.game.Weapon;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaData;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaItem;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaPool;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaType;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.teafx.utils.AnchorPaneUtil;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class GameGachaView implements FxmlView<GameGachaViewModel>, Initializable {

    private static final String IMG_BASE_TALL = "https://webstatic.tajiduo.com/bbs/yh-game-records-web-source/character/tall/";
    private static final String IMG_BASE_FORK = "https://webstatic.tajiduo.com/bbs/yh-game-records-web-source/character/fork/";
    private static final String AVATAR_BASE = "https://webstatic.tajiduo.com/bbs/yh-game-records-web-source/avatar/square/";
    private static final ConcurrentHashMap<String, Image> imageCache = new ConcurrentHashMap<>();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @InjectViewModel
    private GameGachaViewModel viewModel;

    @FXML private VBox contentPane;
    @FXML private VBox emptyPane;
    @FXML private ComboBox<TaygedoAccount> accountCombo;
    @FXML private Button refreshBtn;
    @FXML private Label statusLabel;
    @FXML private HBox playerInfoPane;
    @FXML private ImageView avatarView;
    @FXML private Label roleNameLabel;
    @FXML private Label levelLabel;
    @FXML private Label luckLabel;
    @FXML private HBox poolCardsPane;
    @FXML private ProgressIndicator loadingIndicator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playerInfoPane.setVisible(false);
        playerInfoPane.setManaged(false);

        accountCombo.setItems(viewModel.getAccountList());
        accountCombo.setCellFactory(param -> new AccountListCell());
        accountCombo.setButtonCell(new AccountListCell());
        viewModel.selectedAccountProperty().bindBidirectional(accountCombo.valueProperty());

        contentPane.visibleProperty().bind(Bindings.isNotEmpty(viewModel.getAccountList()));
        contentPane.managedProperty().bind(Bindings.isNotEmpty(viewModel.getAccountList()));
        emptyPane.visibleProperty().bind(Bindings.isEmpty(viewModel.getAccountList()));
        emptyPane.managedProperty().bind(Bindings.isEmpty(viewModel.getAccountList()));

        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());

        viewModel.statusMessageProperty().addListener((obs, old, val) -> {
            boolean hasMsg = val != null && !val.isEmpty();
            statusLabel.setVisible(hasMsg);
            statusLabel.setManaged(hasMsg);
            statusLabel.setText(val);
        });

        viewModel.gachaDataProperty().addListener((obs, old, data) -> {
            if (data != null) {
                buildPlayerInfo();
                buildPoolCards(data);
            } else {
                playerInfoPane.setVisible(false);
                playerInfoPane.setManaged(false);
                poolCardsPane.getChildren().clear();
            }
        });

        viewModel.initialize();
    }

    @FXML
    private void onRefresh() {
        viewModel.onRefresh();
    }

    private void buildPlayerInfo() {
        String av = viewModel.avatarProperty().get();
        if (av != null && !av.isEmpty()) {
            avatarView.setImage(imageCache.computeIfAbsent("avatar_" + av,
                    k -> new Image(AVATAR_BASE + av + ".PNG", 48, 48, true, true, true)));
        }
        roleNameLabel.setText(viewModel.roleNameProperty().get() != null ? viewModel.roleNameProperty().get() : "");
        levelLabel.setText("Lv." + viewModel.levelProperty().get());
        luckLabel.setText(viewModel.luckTitleProperty().get() != null ? viewModel.luckTitleProperty().get() : "");
        playerInfoPane.setVisible(true);
        playerInfoPane.setManaged(true);
    }

    private void buildPoolCards(LocalGachaData data) {
        poolCardsPane.getChildren().clear();
        if (data.getPools() == null || data.getPools().isEmpty()) return;

        for (LocalGachaPool pool : data.getPools()) {
            poolCardsPane.getChildren().add(createPoolCard(pool));
        }
    }

    private VBox createPoolCard(LocalGachaPool pool) {
        VBox card = new VBox(5);
        card.getStyleClass().add("pool-card");
        card.setPadding(new Insets(12));
        HBox.setHgrow(card, Priority.ALWAYS);

        VBox header = new VBox(2);

        Label tabLabel = new Label(pool.getPoolName());
        tabLabel.getStyleClass().add("pool-title");

        Label countLabel = new Label(String.valueOf(pool.getCount()));
        countLabel.getStyleClass().add("pool-count");
        HBox titleHbox = new HBox(tabLabel, new Spacer(), countLabel);

        Label timeLabel = new Label(pool.getTime() != null ? pool.getTime() : "");
        timeLabel.getStyleClass().add("pool-date");

        header.getChildren().addAll(titleHbox, timeLabel);

        VBox center = new VBox(5);

        float ssrPercent = pool.getCount() > 0 ? (float) pool.getSsrCount() / (float) pool.getCount() : 0;
        Label ssrLabel = new Label("S级数量");
        int itemCount = pool.getItems() != null ? pool.getItems().size() : 0;
        Label ssrCountLabel = new Label(String.format("%d[%.2f]", itemCount, ssrPercent));
        HBox ssrHbox = new HBox(ssrLabel, new Spacer(), ssrCountLabel);
        ssrHbox.getStyleClass().add("pool-stats");

        Label ssrAvgLabel = new Label("S级平均");
        Label ssrAvgCountLabel = new Label(String.format("%.2f", pool.getSsrAvg()));
        HBox ssrAvgHbox = new HBox(ssrAvgLabel, new Spacer(), ssrAvgCountLabel);
        ssrAvgHbox.getStyleClass().add("pool-stats");

        Label statLabel = new Label(String.format("总计 %d 抽 | 五星 %d 个 | 平均 %.1f 抽 | %s",
                pool.getCount(), pool.getSsrCount(),
                pool.getSsrAvg(), luckyTypeName(pool.getLuckyType())));
        statLabel.getStyleClass().add("pool-stats");



        if (pool.getType() == LocalGachaType.WEAPON_POOL) {
            int upTotal = (int) pool.getUpSsrCount() + (int) pool.getNoUpSsrCount();
            float upPercent = upTotal > 0 ? (float) pool.getUpSsrCount() / (float) upTotal : 0;
            Label upLabel = new Label("限定数量");
            Label upCountLabel = new Label(String.format("%.0f[%.2f]", pool.getUpSsrCount(), upPercent));
            HBox upHbox = new HBox(upLabel, new Spacer(), upCountLabel);
            upHbox.getStyleClass().add("pool-stats");

            Label upAvgLabel = new Label("限定平均");
            Label upAvgCountLabel = new Label(String.format("%.2f", pool.getUpSsrAvg()));
            HBox upAvgHbox = new HBox(upAvgLabel, new Spacer(), upAvgCountLabel);
            upAvgHbox.getStyleClass().add("pool-stats");

            Label noUpLabel = new Label("常驻数量");
            Label noUpCountLabel = new Label(String.valueOf((int) pool.getNoUpSsrCount()));
            HBox noUpHbox = new HBox(noUpLabel, new Spacer(), noUpCountLabel);
            noUpHbox.getStyleClass().add("pool-stats");

            center.getChildren().addAll(ssrHbox,upHbox, noUpHbox,upAvgHbox,ssrAvgHbox );
        }else {
            center.getChildren().addAll(ssrHbox, ssrAvgHbox);
        }

        ListView<LocalGachaItem> listView = new ListView<>();
        listView.getStyleClass().add("gacha-list");
        if (pool.getItems() != null) {
            listView.setItems(FXCollections.observableArrayList(pool.getItems()));
        }
        listView.setCellFactory(param -> new GachaItemListCell(pool));
        listView.setPrefHeight(200);
        VBox.setVgrow(listView, Priority.ALWAYS);
        Separator sep = new Separator();
        card.getChildren().addAll(header, sep, center, listView);
        return card;
    }

    /** 获取角色立绘图片（缓存 + 后台加载），弧盘池使用 fork 路径 */
    private static Image getCharacterImage(String charId) {
        return imageCache.computeIfAbsent(charId, id -> {
            String base = id.startsWith("fork_") ? IMG_BASE_FORK : IMG_BASE_TALL;
            String ext = id.startsWith("fork_") ? ".png" : ".PNG";
            return new Image(base + id + ext, 0, 42, true, true, true);
        });
    }

    /** ListView 单元格：角色立绘 + 保底信息 */
    class GachaItemListCell extends ListCell<LocalGachaItem> {
        private final BorderPane root;
        private final ImageView iv;
        private final Label name;
        private final Label date;
        private final Label count;
        private final ProgressBar progressBar;
        private final Label desc;
        private final LocalGachaPool pool;

        public GachaItemListCell(LocalGachaPool pool) {
            this.pool = pool;
            root = new BorderPane();

            iv = new ImageView();
            iv.setFitHeight(42);
            iv.setFitWidth(42);
            iv.setSmooth(true);

            name = new Label();
            name.getStyleClass().add("role-name");
            date = new Label();
            date.getStyleClass().add("role-date");
            VBox center = new VBox(name, date);
            center.setPadding(new Insets(0, 0, 0, 10));
            center.setAlignment(Pos.CENTER_LEFT);

            desc = new Label();
            desc.getStyleClass().add("role-desc");
            count = new Label();
            count.getStyleClass().add("role-name");
            HBox left = new HBox(5.0, count);
            left.setAlignment(Pos.CENTER_RIGHT);

            progressBar = new ProgressBar();
            progressBar.setProgress(0);
            AnchorPane bottom = new AnchorPane(progressBar);
            AnchorPaneUtil.setPosition(progressBar, 0);

            root.setLeft(iv);
            root.setCenter(center);
            root.setRight(left);
            root.setBottom(bottom);

            root.getStyleClass().addAll("role-cell");
        }

        @Override
        protected void updateItem(LocalGachaItem item, boolean b) {
            super.updateItem(item, b);
            if (!b) {
                iv.setImage(getCharacterImage(item.getCharid()));
                if (pool.getType() != LocalGachaType.WEAPON_POOL) {
                    Optional<Character> characterOpt = viewModel.getCharacter(item.getCharid());
                    characterOpt.ifPresent(character -> name.setText(character.getZh()));
                } else {
                    Optional<Weapon> weaponOpt = viewModel.getWeapon(item.getCharid());
                    weaponOpt.ifPresent(weapon -> name.setText(weapon.getZh().replace("「", "").replace("」", "")));
                }
                date.setText(DATE_FMT.format(Instant.ofEpochMilli(item.getTimeStamp())));
                count.setText(String.format("%02d", item.getRareCount()));
                progressBar.setProgress((double) item.getRareCount() / pool.getMax());

                if (item.isUp()) {
                    count.getStyleClass().removeAll("unup");
                    count.getStyleClass().add("up");
                    progressBar.getStyleClass().removeAll("unup");
                    progressBar.getStyleClass().add("up");
                } else {
                    count.getStyleClass().removeAll("up");
                    count.getStyleClass().add("unup");
                    progressBar.getStyleClass().removeAll("up");
                    progressBar.getStyleClass().add("unup");
                }
                setGraphic(root);
            } else {
                setGraphic(null);
            }
        }
    }


    private static String luckyTypeName(int luckyType) {
        return switch (luckyType) {
            case 1 -> "大非";
            case 2 -> "小非";
            case 3 -> "一般";
            case 4 -> "欧";
            case 5 -> "超欧";
            default -> "";
        };
    }

    private static class AccountListCell extends ListCell<TaygedoAccount> {
        @Override
        protected void updateItem(TaygedoAccount item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                setText(item.getPhone() + (item.getName() != null ? " (" + item.getName() + ")" : ""));
            } else {
                setText(null);
            }
        }
    }
}
