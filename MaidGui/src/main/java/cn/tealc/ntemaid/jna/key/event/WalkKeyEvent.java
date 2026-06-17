package cn.tealc.ntemaid.jna.key.event;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.jna.key.GlobalKeyListenManager;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 设置人物自动行走
 *
 * @author leck
 * @date 2026/06/16
 */
public class WalkKeyEvent implements KeyEvent {
    private static final Logger log = LoggerFactory.getLogger(WalkKeyEvent.class);
    private final Robot robot = new Robot();
    private boolean isAutoWalking = false;

    @Override
    public void accept(int keyCode, GlobalKeyListenManager manager) {
        if (!Config.getSetting().isGameAutoWalking())
            return;
        if (keyCode == NativeKeyEvent.VC_CAPS_LOCK) {
            if (!isAutoWalking) {
                log.debug("【自动移动】通过 Robot 模拟按住 W");
                isAutoWalking = true;
                Platform.runLater(() -> {
                    // 极其重要：告诉管理器接下来是 Robot 触发的，防止死循环
                    manager.setRobotOperating(true);
                    robot.keyPress(KeyCode.W);
                    manager.setRobotOperating(false);
                });
            } else {
                // 关闭自动走：再次按下 Caps Lock
                log.debug("【自动移动】手动关闭，释放 W");
                isAutoWalking = false;
                Platform.runLater(() -> {
                    manager.setRobotOperating(true);
                    robot.keyRelease(KeyCode.W);
                    manager.setRobotOperating(false);
                });
            }
            return;
        }

        // 2. 打断机制：自动走期间，如果玩家自己按了 S（后退）或 W，则立马松开并退出状态
        if (isAutoWalking) {
            if (keyCode == NativeKeyEvent.VC_S || keyCode == NativeKeyEvent.VC_W) {
                log.debug("【自动移动】检测到玩家手动介入，释放自动移动");
                isAutoWalking = false;
                Platform.runLater(() -> {
                    manager.setRobotOperating(true);
                    robot.keyRelease(KeyCode.W);
                    manager.setRobotOperating(false);
                });
            }
        }
    }
}