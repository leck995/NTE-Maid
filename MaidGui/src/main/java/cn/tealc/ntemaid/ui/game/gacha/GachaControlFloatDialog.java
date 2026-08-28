package cn.tealc.ntemaid.ui.game.gacha;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.thread.gacha.GachaTask;
import cn.tealc.ntemaid.ui.component.dialog.NewDialog;
import cn.tealc.teafx.utils.message.MessageInfo;
import javafx.animation.RotateTransition;
import javafx.concurrent.Worker;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

import java.io.File;
import java.net.URL;

/**
 * 异环抽卡记录抓取控制对话框（标准窗口模式）。
 *
 * <p>继承 {@link NewDialog}，使用应用统一的 shadcn 风格皮肤（自带 HeaderBar
 * 标题与关闭按钮），owner 为主窗口，{@link Modality#NONE} 非模态显示，
 * 保证在所有机器上均可正常显示。
 *
 * <p>功能与 {@link GachaControlDialog} 一致：控制 {@link GachaTask} 抓取任务
 * 的开始与结束，并通过状态图标实时反映任务状态。区别在于本类以标准窗口
 * 形态呈现（有标题栏、可拖动、可置前），而非透明无边框的悬浮窗，因此
 * 不需要透明宿主窗口，规避了部分环境下悬浮窗不显示的问题。
 */
public class GachaControlFloatDialog extends NewDialog<Void> {

    private static final String CSS_PATH =
            "/cn/tealc/ntemaid/css/GachaTool.css";

    private static final String STATUS_RUNNING = "gacha-control-status-running";
    private static final String STATUS_READY = "gacha-control-status-ready";
    private static final String STATUS_DONE = "gacha-control-status-done";
    private static final String STATUS_FAILED = "gacha-control-status-failed";

    private GameGachaCommonViewModel viewModel;
    private String playerId;

    private final VBox contentRoot = new VBox();
    private final FontIcon statusIcon = new FontIcon();
    private final Label modeText = new Label();
    private final RadioButton autoRadio = new RadioButton("自动");
    private final RadioButton manualRadio = new RadioButton("手动");
    private final ToggleGroup modeGroup = new ToggleGroup();
    private final Button startBtn = new Button("开始");
    private final Button stopBtn = new Button("结束");

    private GachaTask currentTask;
    /** 运行中图标旋转动画 */
    private final RotateTransition iconSpin = new RotateTransition(Duration.millis(1000), statusIcon);
    /** 标记本次抓取是否由用户手动停止 */
    private boolean manualStopped = false;

    public GachaControlFloatDialog() {
        super();

        // 非模态：不阻塞主窗口，与 GachaControlDialog（独立 Stage）行为一致
        initModality(Modality.NONE);
        setTitle("异环抽卡记录抓取工具");
        setWidth(420.0);

        // 保留一个 CANCEL 类型按钮以满足 Dialog.close() 的关闭许可校验
        // （DialogPane 无 ButtonType 时 close() 会被拦截，导致窗口无法关闭），
        // 显示后立即将其隐藏，开始/结束按钮由 content 内自定义节点提供。
        getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL);
        setResultConverter(buttonType -> null);
        setOnShown(e -> {
            Button cancelBtn = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
            if (cancelBtn != null) {
                cancelBtn.setVisible(false);
                cancelBtn.setManaged(false);
            }
        });

        buildUI();

        // 加载抓取控制窗样式表
        URL stylesheet = getClass().getResource(CSS_PATH);
        if (stylesheet != null) {
            String external = stylesheet.toExternalForm();
            if (!getDialogPane().getStylesheets().contains(external)) {
                getDialogPane().getStylesheets().add(external);
            }
        }

        getDialogPane().setContent(contentRoot);

        // 窗口隐藏时清理：停止旋转动画与正在运行的任务
        setOnHidden(e -> {
            iconSpin.stop();
            if (currentTask != null && currentTask.isRunning()) {
                currentTask.stop();
            }
        });
    }

    private void buildUI() {
        contentRoot.getStyleClass().add("gacha-control-content");
        setStatusClass(STATUS_READY);

        // ---- 第1行：模式选择 ----（标题与关闭按钮由 HeaderBar 提供）
        autoRadio.setToggleGroup(modeGroup);
        manualRadio.setToggleGroup(modeGroup);
        autoRadio.setSelected(true);
        // 切换模式时若处于就绪态，同步更新提示文本
        autoRadio.selectedProperty().addListener((obs, wasAuto, isAuto) -> {
            if (currentTask == null || !currentTask.isRunning()) {
                updateReadyText();
            }
        });

        HBox header = new HBox(8.0, new Spacer(), autoRadio, manualRadio);
        header.getStyleClass().add("gacha-control-header");

        // ---- 第2行：状态图标 + 模式文本 + 开始/结束 ----
        statusIcon.getStyleClass().add("gacha-control-icon");
        statusIcon.setIconCode(Material2MZ.RADIO_BUTTON_UNCHECKED);

        // 运行中图标旋转动画（无限循环，停止后手动停掉）
        iconSpin.setByAngle(360);
        iconSpin.setCycleCount(RotateTransition.INDEFINITE);
        iconSpin.setInterpolator(javafx.animation.Interpolator.LINEAR);

        updateReadyText();

        startBtn.getStyleClass().add(Styles.ACCENT);
        startBtn.setOnAction(e -> startCapture());

        stopBtn.getStyleClass().add(Styles.DANGER);
        stopBtn.setDisable(true);
        stopBtn.setOnAction(e -> {
            if (currentTask != null) {
                manualStopped = true;
                modeText.setText("正在停止...");
                currentTask.stop();
            }
        });

        HBox row = new HBox(8.0, statusIcon, modeText, new Spacer(), startBtn, stopBtn);
        row.getStyleClass().add("gacha-control-row");

        contentRoot.getChildren().addAll(header, row);
    }

    /**
     * 注入依赖与玩家ID，并把窗口重置为就绪态。每次显示前调用。
     * 同步更新 ViewModel 的 selectedPlayerId，保证抓取完成事件能正确归位。
     */
    public void configure(GameGachaCommonViewModel viewModel, String playerId) {
        this.viewModel = viewModel;
        this.playerId = playerId;
        if (playerId != null && !playerId.isEmpty()) {
            viewModel.selectedPlayerIdProperty().set(playerId);
        }
        resetToReady();
    }

    private void startCapture() {
        if (viewModel == null || playerId == null || playerId.isEmpty()) {
            return;
        }
        boolean autoPage = autoRadio.isSelected();
        manualStopped = false;

        // 任务由本控制窗直接创建与驱动，完成时通过通知把结果文件交给 ViewModel 导入
        currentTask = new GachaTask(autoPage);
        currentTask.setOnSucceeded(e -> {
            File capturedFile = currentTask.getValue();
            if (capturedFile != null && capturedFile.exists()) {
                NotificationManager.publish(NotificationKey.GACHA_CAPTURE_FINISHED, playerId, capturedFile);
            } else {
                NotificationManager.message(MessageInfo.error("抓取失败：未获取到抽卡数据"));
            }
        });
        currentTask.setOnFailed(e ->
                NotificationManager.message(MessageInfo.error("抓取失败：" + currentTask.getException().getMessage())));
        Thread.startVirtualThread(currentTask);

        // 运行态：图标/按钮/模式锁定
        setStatusClass(STATUS_RUNNING);
        statusIcon.setIconCode(Material2AL.AUTORENEW);
        iconSpin.playFromStart();
        modeText.setText("正在抓取...");
        startBtn.setDisable(true);
        stopBtn.setDisable(false);
        autoRadio.setDisable(true);
        manualRadio.setDisable(true);

        // 任务结束时按结果复位
        currentTask.runningProperty().addListener((obs, wasRunning, running) -> {
            if (!running) {
                iconSpin.stop();
                resetToReady();
                Worker.State state = currentTask.getState();
                if (state == Worker.State.SUCCEEDED) {
                    setStatusClass(STATUS_DONE);
                    statusIcon.setIconCode(Material2AL.CHECK_CIRCLE);
                    modeText.setText(manualStopped ? "已手动停止" : "抓取完成");
                } else if (state == Worker.State.FAILED || state == Worker.State.CANCELLED) {
                    setStatusClass(STATUS_FAILED);
                    statusIcon.setIconCode(Material2AL.CANCEL);
                    modeText.setText("抓取失败");
                }
            }
        });
    }

    /** 恢复为就绪态：图标/按钮/模式解锁，并显示就绪提示 */
    private void resetToReady() {
        statusIcon.setIconCode(Material2MZ.RADIO_BUTTON_UNCHECKED);
        setStatusClass(STATUS_READY);
        updateReadyText();
        startBtn.setDisable(false);
        stopBtn.setDisable(true);
        autoRadio.setDisable(false);
        manualRadio.setDisable(false);
    }

    /** 根据当前选中的模式显示就绪文本 */
    private void updateReadyText() {
        modeText.setText(autoRadio.isSelected() ? "自动抓取就绪" : "手动抓取就绪");
    }

    private void setStatusClass(String statusClass) {
        contentRoot.getStyleClass().removeAll(STATUS_RUNNING, STATUS_READY, STATUS_DONE, STATUS_FAILED);
        contentRoot.getStyleClass().add(statusClass);
    }
}
