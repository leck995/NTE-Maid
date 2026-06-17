package cn.tealc.ntemaid.thread.game.log.event;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.jna.key.GlobalKeyListenManager;
import cn.tealc.ntemaid.jna.key.event.BankKeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class BankActivityEvent implements Consumer<String> {
    private static final String TRANSFER_1F= "transferData=Bankrobery_transfer_1F"; //移动到1f,标志粉爪正式开始
    private static final String exist = "UHTUI_CloneSystemExit::OnClickSure RequestExit, CloneInfo:RobBank, RobBank";//结束
    private static final String exist2 = "RemoveCloneMian=RobBankMain, (RobBank,RobBank)";//结束

    private static final Logger log = LoggerFactory.getLogger(BankActivityEvent.class);
    private final BankKeyEvent event;

    public BankActivityEvent() {
        event = new BankKeyEvent();
    }

    @Override
    public void accept(String s) {
        if (!Config.getSetting().isGameBankFEnhance())
            return;

        if (s.contains(TRANSFER_1F)){
            log.debug("进入粉爪大劫案");
            GlobalKeyListenManager.getInstance().register(event);
        }else if (s.contains(exist) || s.contains(exist2)){
            log.debug("离开粉爪大劫案");
            GlobalKeyListenManager.getInstance().unregister(event);
        }
    }
}
