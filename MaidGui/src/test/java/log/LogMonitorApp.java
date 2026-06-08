package log;

import cn.tealc.ntemaid.thread.game.log.LogMonitorTask;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LogMonitorApp extends Application {

    private LogMonitorTask logService;
    private final Path TARGET_LOG_PATH = Paths.get(System.getenv("LOCALAPPDATA"), "HT", "Saved", "Logs", "HT.log");

    @Override
    public void start(Stage primaryStage) {
        // --- 1. UI 组件初始化 ---
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: 'Consolas', 'Monospaced'; " +
                "-fx-control-inner-background: #1e1e1e; " +
                "-fx-text-fill: #dcdcdc;");

        // 按钮栏
        Button pauseBtn = new Button("暂停");
        Button clearBtn = new Button("清空日志");

        HBox controls = new HBox(10, pauseBtn, clearBtn);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(controls, textArea);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        // --- 2. 日志服务逻辑 ---
        logService = new LogMonitorTask(TARGET_LOG_PATH);

        // 监听消息并追加
        logService.messageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                textArea.appendText(newVal);
            }
        });

        // --- 3. 按钮事件处理 ---

        // 暂停/恢复切换
        pauseBtn.setOnAction(e -> {
            // ScheduledService 的状态管理
            if (logService.isRunning()) {
                logService.cancel(); // 停止调度
                pauseBtn.setText("恢复");
                textArea.appendText("[系统] 监控已暂停...\n");
            } else {
                logService.restart(); // 重新启动调度
                pauseBtn.setText("暂停");
                textArea.appendText("[系统] 监控已恢复...\n");
            }
        });

        // 清空按钮
        clearBtn.setOnAction(e -> {
            textArea.clear();
        });

        // --- 4. 启动与关闭 ---
        logService.start();

        primaryStage.setTitle("Log Monitor - NteMaid Thread");
        primaryStage.setScene(new Scene(root, 900, 500));

        primaryStage.setOnCloseRequest(event -> {
            if (logService != null) {
                logService.cancel();
            }
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}