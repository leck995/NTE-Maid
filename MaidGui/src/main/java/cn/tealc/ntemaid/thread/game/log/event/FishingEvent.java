package cn.tealc.ntemaid.thread.game.log.event;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.jna.Win32KeySender;
import javafx.util.Duration;

import java.util.function.Consumer;

public class FishingEvent implements Consumer<String> {
    private final Win32KeySender win32KeySender;
    private boolean fishing = false; //因为进入钓鱼界面会触发一次FISHING_FINISH，所以加个变量判定是否是钓鱼后触发
    private int finished = 0;

    private static final String FISHING_START = "CurrFishingState = FISHING_TYPE_THROWROD";
    private static final String FISHING_BAIT = "CurrFishingState = FISHING_TYPE_BAIT";
    private static final String FISHING_FINISH = "CurrFishingState = FISHING_TYPE_SELECTPOINT";


    public FishingEvent() {
        win32KeySender = new Win32KeySender();
    }

    @Override
    public void accept(String row) {
        if (!Config.setting.isFishing())
            return;
        win32KeySender.reGetHwnd();
        if (row.contains(FISHING_START)){
            fishing = true;
            finished = 0;
        }else if (row.contains(FISHING_BAIT)){
            if (Config.setting.isFishingBait()) {
                win32KeySender.clickKey(Win32KeySender.VirtualKey.F,Duration.millis(500));
            }
        }else if (row.contains(FISHING_FINISH)){
            if (Config.setting.isFishingFinish() && fishing) {
                if (finished == 0) {
                    finished += 1;
                } else if (finished == 1) {
                    win32KeySender.clickKey(Win32KeySender.VirtualKey.ESC,Duration.millis(1000));
                    fishing = false;
                }
            }
        }
    }

}
