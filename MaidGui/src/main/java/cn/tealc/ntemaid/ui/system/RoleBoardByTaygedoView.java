package cn.tealc.ntemaid.ui.system;

import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.taygedo.model.RoleHome;
import cn.tealc.taygedo.model.RoleHomeAchieveProgress;
import cn.tealc.teafx.utils.message.MessageInfo;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.MvvmFX;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 角色面板视图（三段式纵向布局），显示塔吉多角色综合面板数据。
 * <p>顶部：个人标识区（头像、昵称、等级、UID+复制、切换/刷新按钮）
 * <br>中部：核心数据网格（3列2行：活跃天数/角色数/成就/猎人/大亨/服务器）
 * <br>底部：实时动态卡片（体力/活力数值 + 活跃度/周本深色胶囊）
 * <p>嵌入到 HomeView 右侧区域。
 */
public class RoleBoardByTaygedoView implements FxmlView<RoleBoardByTaygedoViewModel>, Initializable {

    @InjectViewModel
    private RoleBoardByTaygedoViewModel viewModel;

    @FXML private VBox contentPane;
    @FXML private VBox emptyPane;
    @FXML private Label statusLabel;

    // 顶部：个人标识
    @FXML private ImageView avatarView;
    @FXML private Label roleDisplayLabel;
    @FXML private Label levelLabel;
    @FXML private Label uidLabel;
    @FXML private Button copyUidBtn;
    @FXML private Button switchBtn;
    @FXML private Button refreshBtn;
    @FXML private FontIcon refreshIcon;

    // 中部：核心数据网格
    @FXML private Label loginDaysLabel;
    @FXML private Label charCountLabel;
    @FXML private Label achievementLabel;
    @FXML private Label worldLevelLabel;
    @FXML private Label tycoonLevelLabel;
    @FXML private Label serverLabel;

    // 底部：实时动态
    @FXML private ImageView staminaIconView;
    @FXML private ImageView cityStaminaIconView;
    @FXML private Label staminaLabel;
    @FXML private Label cityStaminaLabel;
    @FXML private Label activityLabel;
    @FXML private Label weeklyCountLabel;
    @FXML private HBox activityCapsule;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 头像绑定
        avatarView.imageProperty().bind(viewModel.avatarImageProperty());
        // 角色名绑定
        roleDisplayLabel.textProperty().bind(viewModel.roleDisplayProperty());
        // 体力/活力图标绑定
        staminaIconView.imageProperty().bind(viewModel.staminaIconProperty());
        cityStaminaIconView.imageProperty().bind(viewModel.cityStaminaIconProperty());

        // 内容区/空状态切换
        contentPane.visibleProperty().bind(viewModel.roleHomeProperty().isNotNull());
        contentPane.managedProperty().bind(viewModel.roleHomeProperty().isNotNull());
        emptyPane.visibleProperty().bind(viewModel.roleHomeProperty().isNull());
        emptyPane.managedProperty().bind(viewModel.roleHomeProperty().isNull());

        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // 监听 roleHome 数据变化，更新界面
        viewModel.roleHomeProperty().addListener((obs, old, result) -> {
            if (result != null) updateLabels(result);
        });

        viewModel.initialize();
    }

    /**
     * 根据 RoleHome 数据更新全部界面 Label
     */
    private void updateLabels(RoleHome result) {
        levelLabel.setText("Lv." + result.getLev());
        uidLabel.setText("UID: " + result.getRoleid());

        // 中部：核心数据网格
        loginDaysLabel.setText(String.valueOf(result.getRoleLoginDays()));
        charCountLabel.setText(String.valueOf(result.getCharidCnt()));

        RoleHomeAchieveProgress achieve = result.getAchieveProgress();
        if (achieve != null) {
            achievementLabel.setText(achieve.getAchievementCnt() + "/" + achieve.getTotal());
        } else {
            achievementLabel.setText("0/0");
        }

        worldLevelLabel.setText(String.valueOf(result.getWorldLevel()));
        tycoonLevelLabel.setText(String.valueOf(result.getTycoonLevel()));
        serverLabel.setText(result.getServername());

        // 底部：实时动态
        staminaLabel.setText(result.getStaminaValue() + "/" + result.getStaminaMaxValue());
        cityStaminaLabel.setText(result.getCityStaminaValue() + "/" + result.getCityStaminaMaxValue());
        activityLabel.setText(result.getDayValue() + "/100");
        weeklyCountLabel.setText(result.getWeekCopiesRemainCnt() + "/3");

        // 活跃度满值(100)时数字变红
        if (result.getDayValue() >= 100) {
            activityCapsule.getStyleClass().add("full");
        } else {
            activityCapsule.getStyleClass().remove("full");
        }
    }

    /**
     * 弹出账号切换下拉菜单
     */
    @FXML
    private void onSwitchAccount() {
        ContextMenu menu = new ContextMenu();
        for (TaygedoAccount account : viewModel.getAccountList()) {
            String label = account.getRoleName() != null && !account.getRoleName().isBlank()
                    ? account.getRoleName() + "  #" + account.getRoleId()
                    : account.getPhone();
            CustomMenuItem item = new CustomMenuItem(new Label(label));
            item.setHideOnClick(true);
            item.setOnAction(e -> {
                viewModel.selectedAccountProperty().set(account);
            });
            menu.getItems().add(item);
        }
        menu.show(switchBtn, switchBtn.localToScreen(0, switchBtn.getHeight()).getX(),
                switchBtn.localToScreen(0, switchBtn.getHeight()).getY());
    }

    /**
     * 复制 UID（角色ID）到系统剪贴板
     */
    @FXML
    private void copyUid() {
        RoleHome home = viewModel.roleHomeProperty().get();
        if (home != null && home.getRoleid() != null) {
            ClipboardContent content = new ClipboardContent();
            content.putString(home.getRoleid());
            Clipboard.getSystemClipboard().setContent(content);
            MvvmFX.getNotificationCenter().publish(
                    NotificationKey.MESSAGE,
                    MessageInfo.success("已复制 UID: " + home.getRoleid()));
        }
    }

    /**
     * 刷新数据，同时播放刷新按钮旋转动画
     */
    @FXML
    private void onRefresh() {
        // 按钮图标旋转动画
        RotateTransition rotate = new RotateTransition(Duration.seconds(0.8), refreshIcon);
        rotate.setByAngle(360);
        rotate.setCycleCount(1);
        rotate.play();
        viewModel.refresh();
    }
}
