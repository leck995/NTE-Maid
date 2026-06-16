package cn.tealc.ntemaid.jna.key.event;

import cn.tealc.ntemaid.jna.key.GlobalKeyListenManager;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankKeyEvent implements KeyEvent {
    private static final Logger log = LoggerFactory.getLogger(BankKeyEvent.class);
    private final Robot robot = new Robot();
    private long lastTime = 0;

    @Override
    public void accept(int keyCode, GlobalKeyListenManager manager) {
        if (keyCode != NativeKeyEvent.VC_W && keyCode != NativeKeyEvent.VC_A &&
                keyCode != NativeKeyEvent.VC_S && keyCode != NativeKeyEvent.VC_D &&
                keyCode != NativeKeyEvent.VC_F) {
            return;
        }

        // 此时，哪怕前面 Robot 模拟了 F 键起落，因为被过滤了，物理 F 依然会稳稳留在这个集合里！
        if (!manager.isKeyPressed(NativeKeyEvent.VC_F)) {
            return;
        }

        long now = System.currentTimeMillis();
        long l = now - lastTime;

        if (l > 1000) {
            log.debug("【天然响应】第一次按F，交给游戏自己处理");
            lastTime = now;
            return;
        }

        // 既然物理按键状态保住了，连点间隔可以适当宽容一点点（比如 150-200ms），给游戏足够的判定反应时间
        if (l < 250) {
            return;
        }

        log.debug("【代码接管】正在自动补发拾取 F... 距离上一次: " + l + "ms");
        lastTime = now;

        Platform.runLater(() -> {
            try {
                // 开启保护：告诉全局监听器接下来的事件是“假的”
                manager.setRobotOperating(true);
                robot.keyPress(KeyCode.F);
                Thread.sleep(15);
                robot.keyRelease(KeyCode.F);

            } catch (InterruptedException e) {
                log.debug(e.getMessage());
            } finally {
                // 极其重要：操作完了务必关闭保护，否则用户的真实键盘后续就无法响应了
                manager.setRobotOperating(false);
            }
        });
    }
}