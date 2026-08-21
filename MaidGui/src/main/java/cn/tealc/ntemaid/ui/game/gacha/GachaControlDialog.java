package cn.tealc.ntemaid.ui.game.gacha;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.jna.GameAppListener;
import cn.tealc.ntemaid.thread.gacha.GachaTask;
import cn.tealc.teafx.utils.message.MessageInfo;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Worker;
import javafx.animation.RotateTransition;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * 异环抽卡记录抓取控制小窗：置顶、透明边框的独立 Stage，负责控制
 * {@link GachaTask} 抓取任务的开始与结束，并通过状态图标实时反映任务状态。
 *
 * <p>本窗口由 {@link GachaToolDialog} 点击「确定」后创建，接替后者承担抓取控制职责。
 * UI 仅两行：第一行为标题 + 自动/手动模式选择 + 关闭按钮；第二行为状态图标、
 * 模式说明文本以及开始/结束按钮。
 */
public class GachaControlDialog extends Stage {
    private static final Logger LOG = LoggerFactory.getLogger(GachaControlDialog.class);

    private static final String CSS_PATH =
            "/cn/tealc/ntemaid/css/GachaTool.css";

    private static final String STATUS_RUNNING = "gacha-control-status-running";
    private static final String STATUS_READY = "gacha-control-status-ready";
    private static final String STATUS_DONE = "gacha-control-status-done";
    private static final String STATUS_FAILED = "gacha-control-status-failed";

    private static GachaControlDialog instance;

    private final SimpleBooleanProperty isVisible = new SimpleBooleanProperty(false);

    private GameGachaCommonViewModel viewModel;
    private String playerId;

    private final VBox root = new VBox();
    private final FontIcon statusIcon = new FontIcon();
    private final Label modeText = new Label();
    private final RadioButton autoRadio = new RadioButton("自动");
    private final RadioButton manualRadio = new RadioButton("手动");
    private final ToggleGroup modeGroup = new ToggleGroup();
    private final Button startBtn = new Button("开始");
    private final Button stopBtn = new Button("结束");
    private final Button closeBtn = new Button(null, new FontIcon(Material2OutlinedAL.CLOSE));

    private GachaTask currentTask;
    /** 运行中图标旋转动画 */
    private final RotateTransition iconSpin = new RotateTransition(Duration.millis(1000), statusIcon);
    /** 标记本次抓取是否由用户手动停止 */
    private boolean manualStopped = false;

    public static GachaControlDialog getInstance() {
        if (instance == null) {
            instance = new GachaControlDialog();
        }
        return instance;
    }

    private GachaControlDialog() {
        // 隐藏任务栏图标的透明宿主窗口
        Stage owner = new Stage();
        owner.setWidth(1.0);
        owner.setHeight(1.0);
        owner.initStyle(StageStyle.UTILITY);
        owner.setOpacity(0.0);
        owner.show();
        initOwner(owner);
        initStyle(StageStyle.TRANSPARENT);

        buildUI();

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        URL stylesheet = getClass().getResource(CSS_PATH);
        if (stylesheet != null && !scene.getStylesheets().contains(stylesheet.toExternalForm())) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
        setScene(scene);
        setAlwaysOnTop(true);

        // 跟踪可见性（show()/hide() 是 final，无法直接覆盖）
        setOnShowing(e -> isVisible.set(true));
        setOnHiding(e -> isVisible.set(false));

        // 窗口首次显示后定位到游戏窗口右上角（窗口修饰器下方）
        addEventHandler(WindowEvent.WINDOW_SHOWN, e -> alignToGameWindow());
    }

    private void buildUI() {
        root.getStyleClass().add("gacha-control-root");
        setStatusClass(STATUS_READY);

        // ---- 第1行：标题 + 模式选择 + 关闭 ----
        Label title = new Label("异环抽卡记录抓取工具");
        title.getStyleClass().add("gacha-control-title");

        autoRadio.setToggleGroup(modeGroup);
        manualRadio.setToggleGroup(modeGroup);
        autoRadio.setSelected(true);
        // 切换模式时若处于就绪态，同步更新提示文本
        autoRadio.selectedProperty().addListener((obs, wasAuto, isAuto) -> {
            if (currentTask == null || !currentTask.isRunning()) {
                updateReadyText();
            }
        });

        closeBtn.getStyleClass().add("gacha-control-close");
        closeBtn.setFocusTraversable(false);
        closeBtn.setOnAction(e -> {
            iconSpin.stop();
            if (currentTask != null && currentTask.isRunning()) {
                currentTask.stop();
            }
            hide();
        });

        HBox header = new HBox(8.0, title, new Spacer(), autoRadio, manualRadio, closeBtn);
        header.getStyleClass().add("gacha-control-header");
        enableDrag(header);

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

        root.getChildren().addAll(header, row);
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

    /**
     * 把本窗口定位到游戏窗口的右上角（与游戏窗口上边缘对齐，右边缘对齐）。
     * <p>游戏可能处于最小化或非前台状态，因此先恢复并置前游戏窗口，
     * 等待其重绘后再读取 {@code GetWindowRect} 进行定位。
     * 游戏未运行时回退到主屏幕右上角。
     */
    private void alignToGameWindow() {
        double dialogWidth = getWidth();

        WinDef.HWND game = GameAppListener.getInstance().getGameHWND();
        if (game != null && User32.INSTANCE.IsWindow(game)) {
            // 先把游戏窗口恢复并置到前台，避免最小化导致定位到错误坐标
            WinUser.WINDOWPLACEMENT placement = new WinUser.WINDOWPLACEMENT();
            if (User32.INSTANCE.GetWindowPlacement(game, placement).booleanValue()
                    && placement.showCmd == WinUser.SW_SHOWMINIMIZED) {
                User32.INSTANCE.ShowWindow(game, WinUser.SW_RESTORE);
            }
            User32.INSTANCE.SetForegroundWindow(game);
            // 给系统一点时间重绘游戏窗口，确保后续读到的矩形准确
            try {
                Thread.sleep(80);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            WinDef.RECT rect = new WinDef.RECT();
            if (User32.INSTANCE.GetWindowRect(game, rect)) {
                double x = rect.left;
                double y = rect.top;
                setX(x);
                setY(y);
                LOG.debug("已定位至游戏窗口右上角: ({}, {})", x, y);
                return;
            }
        }
        // 游戏未运行或读取失败：回退到主屏幕右上角
        double screenX = User32.INSTANCE.GetSystemMetrics(0) - dialogWidth;
        setX(screenX);
        setY(0.0);
        LOG.debug("游戏窗口未检测到，回退至屏幕右上角: ({}, {})", screenX, 0.0);
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
                NotificationManager.publish(NotificationKey.GACHA_CAPTURE_FINISHED,playerId,capturedFile);
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
        root.getStyleClass().removeAll(STATUS_RUNNING, STATUS_READY, STATUS_DONE, STATUS_FAILED);
        root.getStyleClass().add(statusClass);
    }

    /** 为无边框窗口的标题栏实现鼠标拖拽移动 */
    private void enableDrag(HBox header) {
        final Delta drag = new Delta();
        header.setOnMousePressed((MouseEvent e) -> {
            drag.x = e.getScreenX();
            drag.y = e.getScreenY();
        });
        header.setOnMouseDragged((MouseEvent e) -> {
            setX(getX() + e.getScreenX() - drag.x);
            setY(getY() + e.getScreenY() - drag.y);
            drag.x = e.getScreenX();
            drag.y = e.getScreenY();
        });
    }

    private static class Delta {
        double x, y;
    }

    public boolean isVisible() {
        return isVisible.get();
    }

    public SimpleBooleanProperty isVisibleProperty() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible.set(isVisible);
    }
}
