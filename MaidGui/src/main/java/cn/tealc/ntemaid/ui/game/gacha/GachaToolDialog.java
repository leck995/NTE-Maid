package cn.tealc.ntemaid.ui.game.gacha;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import cn.tealc.ntemaid.jna.GameAppListener;
import cn.tealc.ntemaid.model.game.Player;
import cn.tealc.ntemaid.thread.game.log.LoginPlayerGetTask;
import cn.tealc.ntemaid.ui.component.dialog.NewDialog;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽卡抓取配置对话框：负责玩家ID输入、抓取模式选择与使用说明展示。
 * 继承 {@link NewDialog}，采用 shadcn 风格皮肤。点击「确定」后根据所选
 * 模式分别创建 {@link GachaControlDialog}（悬浮窗模式）或
 * {@link GachaControlFloatDialog}（标准窗口模式），由后者接管抓取控制。
 */
public class GachaToolDialog extends NewDialog<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(GachaToolDialog.class);

    private final GameGachaCommonViewModel viewModel;
    private final TextField playerIdField;
    private final Label playerNameLabel;
    /** 抓取模式选择：悬浮窗模式（兼容） / 标准窗口模式（稳定） */
    private final ToggleGroup captureModeGroup = new ToggleGroup();
    private final RadioButton floatModeRadio = new RadioButton("悬浮窗模式（兼容）");
    private final RadioButton standardModeRadio = new RadioButton("标准窗口模式（稳定）");

    public GachaToolDialog(GameGachaCommonViewModel viewModel) {
        super();
        this.viewModel = viewModel;

        setWidth(480.0);
        setTitle("确认");

        // 玩家ID输入
        Label id = new Label("设置游戏UID");
        id.setFont(Font.font(null, FontWeight.BOLD, 15));
        playerNameLabel = new Label();
        playerNameLabel.setFont(Font.font( 14));
        playerNameLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
        HBox group = new HBox(id,new Spacer(),playerNameLabel);



        playerIdField = new TextField();
        if (viewModel.getSelectedPlayerId() != null) {
            playerIdField.setText(viewModel.getSelectedPlayerId());
        }
        playerIdField.setPromptText("输入玩家UID（必填，仅数字）");
        playerIdField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));

        Label tipLabel02 = new Label("UID是您保存的凭证，用于维护后续数据的更新，请确认填写是否正确");
        tipLabel02.getStyleClass().addAll(Styles.TEXT_SUBTLE);
        tipLabel02.setPrefWidth(420);
        tipLabel02.setWrapText(true);

        // 抓取模式选择
        floatModeRadio.setToggleGroup(captureModeGroup);
        standardModeRadio.setToggleGroup(captureModeGroup);
        floatModeRadio.setSelected(true);

        Label modeLabel = new Label("抓取模式");
        modeLabel.setFont(Font.font(null, FontWeight.BOLD, 15));
        HBox modeRow = new HBox(12.0, floatModeRadio, standardModeRadio);
        Label modeTip = new Label("若悬浮窗模式无法显示控制窗口，请改用标准窗口模式，稳定性更高");
        modeTip.getStyleClass().add(Styles.TEXT_SUBTLE);
        modeTip.setPrefWidth(420);
        modeTip.setWrapText(true);


        Label manualLabel = new Label("使用说明");
        manualLabel.setFont(Font.font(null, FontWeight.BOLD, 15));
        manualLabel.setPrefWidth(420);
        manualLabel.setWrapText(true);

        Label tipLabel = new Label("""
                关于自动抓取：
                    开始前请进入抽卡界面（大世界按F3），开始后请勿操作鼠标，否则自动抓取无法工作；推荐将游戏切换到1920*1080分辨率，并确保使用管理员权限启动助手。
                    自动抓取若停在武器历史的最后一页，说明完成抓取，可回到助手查看记录；
                关于手动抓取：
                    你需要对每个角色和武器卡池的历史记录手动翻页，程序会自动记录该页的记录，翻页结束后点击结束按钮完成抓取，可回到助手查看记录；更适合抓取新增数据部分。
                """);
        tipLabel.getStyleClass().addAll(Styles.TEXT_SUBTLE, Styles.TEXT_BOLD);
        tipLabel.setPrefWidth(420);
        tipLabel.setWrapText(true);

        // 原理说明
        Label principleLabel = new Label("原理：通过抓包游戏数据，获取抽卡记录，只读取不修改，更不涉及游戏内存修改，安全性请自行判断");
        principleLabel.getStyleClass().addAll(Styles.TEXT_SUBTLE, Styles.TEXT_BOLD);
        principleLabel.setWrapText(true);
        principleLabel.setPrefWidth(420);
        VBox body = new VBox(8.0,
                group,
                playerIdField,
                tipLabel02,
                new Separator(),
                modeLabel,
                modeRow,
                modeTip,
                new Separator(),
                manualLabel,
                tipLabel, principleLabel);
        getDialogPane().setContent(body);

        // 按钮：确定 / 取消
        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        // 确定按钮：玩家ID为空时禁用
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.getStyleClass().add(Styles.ACCENT);
        okButton.disableProperty().bind(playerIdField.textProperty().isEmpty());

        // 确定 → 根据所选模式创建并显示抓取控制窗
        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String pid = playerIdField.getText().trim();
                if (!pid.isEmpty()) {
                    Stage owner = (Stage) getOwner();
                    if (standardModeRadio.isSelected()) {
                        // 标准窗口模式：以主窗口为 owner，非模态，保证显示
                        // 不最小化主窗口，否则子窗口（Dialog）会一并隐藏
                        GachaControlFloatDialog dialog = new GachaControlFloatDialog();
                        dialog.initOwner(owner);
                        dialog.configure(viewModel, pid);
                        dialog.show();
                    } else {
                        // 悬浮窗模式：先在后台线程恢复最小化的游戏窗口并置前（此时主窗口
                        // 仍在前台，本程序拥有前台权限，SetForegroundWindow 才能成功），
                        // 完成后再回到 UI 线程最小化主窗口、显示悬浮控制窗，避免阻塞 UI 线程
                        final Stage ownerStage = owner;
                        Thread.startVirtualThread(() -> {
                            restoreAndForegroundGameWindow();
                            Platform.runLater(() -> {
                                ownerStage.setIconified(true);
                                GachaControlDialog control = GachaControlDialog.getInstance();
                                control.configure(viewModel, pid);
                                control.show();
                                // 确保悬浮窗渲染完成后出现在最前
                                Platform.runLater(control::toFront);
                            });
                        });
                    }
                }
            }
            return null;
        });

        checkAndSetPlayerId();
    }

    /** 返回当前输入的玩家ID（已 trim） */
    public String getPlayerId() {
        return playerIdField.getText().trim();
    }

    private void checkAndSetPlayerId() {
        LoginPlayerGetTask task = new LoginPlayerGetTask();
        task.setOnSucceeded(event -> {
            Player value = task.getValue();
            playerIdField.setText(String.valueOf(value.getId()));
            playerNameLabel.setText(String.format("已自动识别玩家：%s",value.getName()));
        });
        Thread.startVirtualThread(task);
    }

    /**
     * 在最小化主窗口之前，先恢复并置前游戏窗口。
     * <p>必须在主窗口仍处于前台时调用，此时本程序拥有前台权限，
     * {@link User32#SetForegroundWindow} 才不会被系统拒绝。
     * 否则主窗口最小化后本程序失去前台权限，后续无法把最小化的
     * 游戏窗口拉回前台，导致悬浮控制窗无法对齐显示。
     */
    private void restoreAndForegroundGameWindow() {
        try {
            WinDef.HWND game = GameAppListener.getInstance().getGameHWND();
            if (game == null || !User32.INSTANCE.IsWindow(game)) {
                return;
            }
            // 检测游戏窗口是否处于最小化状态，若是则先恢复
            WinUser.WINDOWPLACEMENT placement = new WinUser.WINDOWPLACEMENT();
            if (User32.INSTANCE.GetWindowPlacement(game, placement).booleanValue()
                    && placement.showCmd == WinUser.SW_SHOWMINIMIZED) {
                User32.INSTANCE.ShowWindow(game, WinUser.SW_RESTORE);
            }
            User32.INSTANCE.SetForegroundWindow(game);
            // 等待游戏窗口重绘完成，使后续读取的窗口矩形准确
            Thread.sleep(300);
            LOG.debug("游戏窗口已恢复并置前");
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOG.warn("恢复并置前游戏窗口失败", e);
        }
    }
}
