package cn.tealc.ntemaid.ui.game.gacha;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import cn.tealc.ntemaid.model.game.Player;
import cn.tealc.ntemaid.thread.game.log.LoginPlayerGetTask;
import cn.tealc.ntemaid.ui.component.dialog.NewDialog;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * 抽卡抓取配置对话框：仅负责玩家ID输入与使用说明展示。
 * 继承 {@link NewDialog}，采用 shadcn 风格皮肤。点击「确定」后
 * 由本对话框内部创建并显示 {@link GachaControlDialog}，
 * 由后者接管抓取控制（自动/手动模式选择与开始/结束）。
 */
public class GachaToolDialog extends NewDialog<Void> {

    private final GameGachaCommonViewModel viewModel;
    private final TextField playerIdField;
    private final Label playerNameLabel;

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


        Label manualLabel = new Label("使用说明");
        manualLabel.setFont(Font.font(null, FontWeight.BOLD, 15));
        manualLabel.setPrefWidth(420);
        manualLabel.setWrapText(true);

        Label tipLabel = new Label("""
                关于自动抓取：
                    开始前请进入抽卡界面（大世界按F3），否则自动抓取无法工作；推荐将游戏切换到1920*1080分辨率，并确保使用管理员权限启动助手。
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
                manualLabel,
                tipLabel, principleLabel);
        getDialogPane().setContent(body);

        // 按钮：确定 / 取消
        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        // 确定按钮：玩家ID为空时禁用
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.getStyleClass().add(Styles.ACCENT);
        okButton.disableProperty().bind(playerIdField.textProperty().isEmpty());

        // 确定 → 创建并显示抓取控制窗
        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String pid = playerIdField.getText().trim();
                if (!pid.isEmpty()) {
                    Stage owner = (Stage) getOwner();
                    owner.setIconified(true);
                    GachaControlDialog control = GachaControlDialog.getInstance();
                    control.configure(viewModel, pid);
                    control.show();
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
}
