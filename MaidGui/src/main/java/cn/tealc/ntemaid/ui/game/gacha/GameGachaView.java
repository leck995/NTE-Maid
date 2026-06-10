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

public class GameGachaView implements FxmlView<GameGachaViewModel>, Initializable {

    private static final String IMG_BASE_TALL = "https://webstatic.tajiduo.com/bbs/yh-game-records-web-source/character/tall/";
    private static final String IMG_BASE_FORK = "https://webstatic.tajiduo.com/bbs/yh-game-records-web-source/character/fork/";
    private static final String AVATAR_BASE = "https://webstatic.tajiduo.com/bbs/yh-game-records-web-source/avatar/square/";
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
    private Label luckHint;
    private VBox luckBox;
    private Label analysisLuckLabel;
    private Label analysisLuckHint;
    private VBox analysisBox;
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

        // 将 luckLabel 从父容器中移除，包装为带提示文本的 VBox 再添加
        luckHint = new Label("近六个月运气");
        luckHint.getStyleClass().add("player-hint");
        ((HBox) luckLabel.getParent()).getChildren().remove(luckLabel);
        luckBox = new VBox(luckLabel, luckHint);
        luckBox.setAlignment(Pos.CENTER);
        playerInfoPane.getChildren().add(luckBox);

        // 创建 analysisLuckLabel 及其提示
        analysisLuckLabel = new Label();
        analysisLuckLabel.getStyleClass().add("player-luck");
        analysisLuckHint = new Label("总运气");
        analysisLuckHint.getStyleClass().add("player-hint");
        analysisBox = new VBox(analysisLuckLabel, analysisLuckHint);
        analysisBox.setAlignment(Pos.CENTER);
        analysisBox.setVisible(false);
        analysisBox.setManaged(false);
        playerInfoPane.getChildren().add(analysisBox);

        viewModel.gachaDataProperty().addListener((obs, old, data) -> {
            if (data != null) {
                buildPlayerInfo();
                buildPoolCards(data);
                analysisLuckLabel.setText(analysisLuckName(data.getLuckyType()));
                analysisBox.setVisible(true);
                analysisBox.setManaged(true);
            } else {
                playerInfoPane.setVisible(false);
                playerInfoPane.setManaged(false);
                poolCardsPane.getChildren().clear();
                luckBox.setVisible(false);
                luckBox.setManaged(false);
                analysisBox.setVisible(false);
                analysisBox.setManaged(false);
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
            avatarView.setImage(viewModel.getImageCacheManager().get(AVATAR_BASE + av + ".PNG", 48, 48, true, true));
        }
        String rn = viewModel.roleNameProperty().get();
        roleNameLabel.setText(rn != null ? rn : "");
        levelLabel.setText("Lv." + viewModel.levelProperty().get());
        String lt = viewModel.luckTitleProperty().get();
        luckLabel.setText(lt != null ? lt : "");

        boolean hasPlayerInfo = rn != null && !rn.isEmpty();
        luckBox.setVisible(hasPlayerInfo);
        luckBox.setManaged(hasPlayerInfo);

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
        Label luckBadge = new Label(luckyTypeName(pool.getLuckyType()));
        luckBadge.getStyleClass().addAll("pool-luck", "luck-" + pool.getLuckyType());
        HBox titleHbox = new HBox(tabLabel, new Spacer(), luckBadge);
        titleHbox.setAlignment(Pos.CENTER);

        Label countLabel = new Label(String.valueOf(pool.getCount()));
        countLabel.getStyleClass().add("pool-count");

        Label timeLabel = new Label(pool.getTime() != null ? pool.getTime() : "");
        timeLabel.getStyleClass().add("pool-date");

        header.getChildren().addAll(titleHbox, countLabel, timeLabel);

        VBox center = new VBox(5);

        float ssrPercent = pool.getCount() > 0 ? (float) pool.getSsrCount() / (float) pool.getCount() : 0;
        Label ssrLabel = new Label("S级数量");
        ssrLabel.getStyleClass().add("ssr-accent");
        int itemCount = pool.getItems() != null ? pool.getItems().size() : 0;
        Label ssrCountLabel = new Label(String.format("%d [%.2f]", itemCount, ssrPercent));
        ssrCountLabel.getStyleClass().add("ssr-accent");
        HBox ssrHbox = new HBox(ssrLabel, new Spacer(), ssrCountLabel);
        ssrHbox.getStyleClass().add("pool-stats");

        Label ssrAvgLabel = new Label("S级平均");
        ssrAvgLabel.getStyleClass().add("ssr-accent");
        Label ssrAvgCountLabel = new Label(String.format("%.2f", pool.getSsrAvg()));
        ssrAvgCountLabel.getStyleClass().add("ssr-accent");
        HBox ssrAvgHbox = new HBox(ssrAvgLabel, new Spacer(), ssrAvgCountLabel);
        ssrAvgHbox.getStyleClass().add("pool-stats");

        if (pool.getType() == LocalGachaType.WEAPON_POOL) {
            int upTotal = (int) pool.getUpSsrCount() + (int) pool.getNoUpSsrCount();
            float upPercent = upTotal > 0 ? (float) pool.getUpSsrCount() / (float) upTotal : 0;
            Label upLabel = new Label("限定数量");
            upLabel.getStyleClass().add("up-accent");
            Label upCountLabel = new Label(String.format("%.0f [%.2f]", pool.getUpSsrCount(), upPercent));
            upCountLabel.getStyleClass().add("up-accent");
            HBox upHbox = new HBox(upLabel, new Spacer(), upCountLabel);
            upHbox.getStyleClass().add("pool-stats");

            Label upAvgLabel = new Label("限定平均");
            upAvgLabel.getStyleClass().add("up-accent");
            Label upAvgCountLabel = new Label(String.format("%.2f", pool.getUpSsrAvg()));
            upAvgCountLabel.getStyleClass().add("up-accent");
            HBox upAvgHbox = new HBox(upAvgLabel, new Spacer(), upAvgCountLabel);
            upAvgHbox.getStyleClass().add("pool-stats");

            Label noUpLabel = new Label("常驻数量");
            noUpLabel.getStyleClass().add("default-accent");
            Label noUpCountLabel = new Label(String.format("%.0f", pool.getNoUpSsrCount()));
            noUpCountLabel.getStyleClass().add("default-accent");
            HBox noUpHbox = new HBox(noUpLabel, new Spacer(), noUpCountLabel);
            noUpHbox.getStyleClass().add("pool-stats");

            center.getChildren().addAll(ssrHbox, ssrAvgHbox, upHbox, upAvgHbox, noUpHbox);
        } else {
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

    private Image getCharacterImage(String charId) {
        String base = charId.startsWith("fork_") ? IMG_BASE_FORK : IMG_BASE_TALL;
        String ext = charId.startsWith("fork_") ? ".png" : ".PNG";
        return viewModel.getImageCacheManager().get(base + charId + ext, 0, 42, true, true);
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
        private final Label upTag;
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
            upTag = new Label("UP!");
            upTag.getStyleClass().add("up-tag");
            count = new Label();
            count.getStyleClass().add("role-name");
            HBox left = new HBox(8.0, upTag, count);
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
                    upTag.setVisible(true);
                    count.getStyleClass().removeAll("unup");
                    count.getStyleClass().add("up");
                    progressBar.getStyleClass().removeAll("unup");
                    progressBar.getStyleClass().add("up");
                } else {
                    upTag.setVisible(false);
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
