package log;

import cn.tealc.ntemaid.thread.game.log.LogMonitorWatchService;
import javafx.application.Application;
import javafx.application.Platform;
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

    private LogMonitorWatchService logService;
    private Thread monitorThread;
    private final Path TARGET_LOG_PATH = Paths.get(System.getenv("LOCALAPPDATA"), "HT", "Saved", "Logs", "HT.log");

    @Override
    public void start(Stage primaryStage) {
        // --- 1. UI ---
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: 'Consolas', 'Monospaced'; " +
                "-fx-control-inner-background: #1e1e1e; " +
                "-fx-text-fill: #dcdcdc;");

        Button pauseBtn = new Button("暂停");
        Button clearBtn = new Button("清空日志");

        HBox controls = new HBox(10, pauseBtn, clearBtn);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(controls, textArea);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        // --- 2. 启动日志监控 ---
        startMonitor(textArea);

        // --- 3. 按钮事件 ---

        pauseBtn.setOnAction(e -> {
            if (logService != null && logService.isRunning()) {
                logService.stop();
                pauseBtn.setText("恢复");
                textArea.appendText("[系统] 监控已暂停...\n");
            } else {
                startMonitor(textArea);
                pauseBtn.setText("暂停");
                textArea.appendText("[系统] 监控已恢复...\n");
            }
        });

        clearBtn.setOnAction(e -> textArea.clear());

        // --- 4. 关闭 ---
        primaryStage.setTitle("Log Monitor - NteMaid Thread");
        primaryStage.setScene(new Scene(root, 900, 500));

        primaryStage.setOnCloseRequest(event -> {
            if (logService != null) {
                logService.stop();
            }
        });

        primaryStage.show();
    }

    private void startMonitor(TextArea textArea) {
        logService = new LogMonitorWatchService(TARGET_LOG_PATH,
                decrypted -> Platform.runLater(() -> textArea.appendText(decrypted + "\n")));
        monitorThread = new Thread(logService);
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
}