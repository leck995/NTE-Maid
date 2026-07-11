package cn.tealc.ntemaid.ui.game.gacha;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import cn.tealc.ntemaid.thread.gacha.GachaTask;
import com.jfoenixN.controls.JFXDialogLayout;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * 抓取抽卡数据对话框，提供开始/结束抓取按钮控制 GachaTask 的执行
 */
public class GachaToolDialog extends JFXDialogLayout {

    private final GameGachaCommonViewModel viewModel;
    private GachaTask currentTask;

    public GachaToolDialog(GameGachaCommonViewModel viewModel) {
        this.viewModel = viewModel;

        Label title = new Label("抓取抽卡数据");
        title.getStyleClass().add(Styles.TITLE_3);
        setHeading(title);

        // 抓取方式选择
        RadioButton manualRadio = new RadioButton("手动抓取");
        RadioButton autoRadio = new RadioButton("自动抓取");
        ToggleGroup modeGroup = new ToggleGroup();
        manualRadio.setToggleGroup(modeGroup);
        autoRadio.setToggleGroup(modeGroup);
        autoRadio.setSelected(true);

        // 模式描述
        Label manualDesc = new Label("玩家需要手动进入角色与弧盘抽卡历史界面一页一页翻取，然后点击结束按钮完成");
        manualDesc.getStyleClass().addAll(Styles.TEXT_SUBTLE);
        manualDesc.setStyle("-fx-font-size: 0.85em;");
        manualDesc.setWrapText(true);
        manualDesc.visibleProperty().bind(manualRadio.selectedProperty());
        manualDesc.managedProperty().bind(manualRadio.selectedProperty());

        Label autoDesc = new Label("全程由程序自动操作，一旦点击开始抓取，请勿操作鼠标或键盘；若是获取增量数据，可手动结束来快速完成");
        autoDesc.getStyleClass().addAll(Styles.TEXT_SUBTLE);
        autoDesc.setStyle("-fx-font-size: 0.85em;");
        autoDesc.setWrapText(true);
        autoDesc.visibleProperty().bind(autoRadio.selectedProperty());
        autoDesc.managedProperty().bind(autoRadio.selectedProperty());

        // 玩家ID输入
        Label id = new Label("设置游戏ID");
        id.getStyleClass().add(Styles.TITLE_3);

        TextField field = new TextField();
        if (viewModel.getSelectedPlayerId() != null) {
            field.setText(viewModel.getSelectedPlayerId());
        }
        field.setPromptText("输入玩家ID（必填，仅数字）");
        field.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));

        Label tipLabel = new Label("开始前请进入抽卡界面（大世界按F3），否则自动抓取无法工作；若自动抓取在当前分辨率无法正常工作，请将游戏切换到1920*1080分辨率");
        tipLabel.getStyleClass().addAll(Styles.TEXT_SUBTLE, Styles.TEXT_BOLD);
        tipLabel.setStyle("-fx-font-size: 1em;");
        tipLabel.setWrapText(true);

        // 原理说明
        Label principleLabel = new Label("原理：通过抓包游戏数据，获取抽卡记录，只读取不修改，更不涉及游戏内存修改，安全性请自行判断");
        principleLabel.getStyleClass().addAll(Styles.TEXT_SUBTLE, Styles.TEXT_BOLD);
        principleLabel.setStyle("-fx-font-size: 1em;");
        principleLabel.setWrapText(true);


        VBox body = new VBox(8.0,
                id,
                field,
                new VBox(4.0, manualRadio, manualDesc),
                new VBox(4.0, autoRadio, autoDesc),
                new Separator(),
                tipLabel, principleLabel);
        setBody(body);

        // 按钮
        Button startBtn = new Button("开始抓取");
        startBtn.getStyleClass().add(Styles.ACCENT);
        startBtn.disableProperty().bind(field.textProperty().isEmpty());

        Button stopBtn = new Button("结束抓取");
        stopBtn.getStyleClass().add(Styles.DANGER);
        stopBtn.setDisable(true);

        startBtn.setOnAction(e -> {
            String playerId = field.getText().trim();
            if (playerId.isEmpty()) return;

            boolean autoPage = autoRadio.isSelected();
            currentTask = viewModel.startGachaCapture(playerId, autoPage);

            // 运行时按钮状态绑定
            startBtn.disableProperty().unbind();
            startBtn.textProperty().bind(
                    Bindings.when(currentTask.runningProperty())
                            .then("正在运行...")
                            .otherwise("开始抓取"));
            startBtn.disableProperty().bind(currentTask.runningProperty());
            stopBtn.disableProperty().bind(currentTask.runningProperty().not());

            // 任务结束时重置按钮
            currentTask.runningProperty().addListener((obs, old, running) -> {
                if (!running) {
                    resetButtons(startBtn, stopBtn, field);
                }
            });
        });

        stopBtn.setOnAction(e -> {
            if (currentTask != null) {
                currentTask.stop();
            }
        });

        Button backBtn = new Button("返回");
        backBtn.setCancelButton(true);


        setActions(startBtn, stopBtn, backBtn);
    }

    /**
     * 重置按钮到初始状态
     */
    private void resetButtons(Button startBtn, Button stopBtn, TextField field) {
        startBtn.textProperty().unbind();
        startBtn.setText("开始抓取");
        startBtn.disableProperty().unbind();
        startBtn.disableProperty().bind(field.textProperty().isEmpty());
        stopBtn.disableProperty().unbind();
        stopBtn.setDisable(true);
    }
}
