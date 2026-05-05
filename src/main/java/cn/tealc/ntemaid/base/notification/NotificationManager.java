package cn.tealc.ntemaid.base.notification;

import cn.tealc.teafx.utils.message.MessageInfo;
import com.jfoenixN.controls.JFXDialogLayout;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.utils.notifications.NotificationObserver;

/**
 * @program: WutheringWavesTool
 * @description:
 * @author: Leck
 * @create: 2024-12-17 17:10
 */
public class NotificationManager {
    public static void publish(String key, Object... objects) {
        MvvmFX.getNotificationCenter().publish(key, objects);
    }

    public static void subscribe(String key, NotificationObserver observer) {
        MvvmFX.getNotificationCenter().subscribe(key, observer);
    }
    public static void unsubscribe(String key, NotificationObserver observer) {
        MvvmFX.getNotificationCenter().unsubscribe(key, observer);
    }
    public static void message(MessageInfo messageInfo) {
        MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE, messageInfo);
    }

    public static void dialog(JFXDialogLayout layout) {
        MvvmFX.getNotificationCenter().publish(NotificationKey.DIALOG, layout);
    }
}