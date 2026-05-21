package cn.tealc.ntemaid.thread.game.log.event;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.service.RobotService;
import cn.tealc.ntemaid.thread.game.log.LogMonitorForMusicTask;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.function.Consumer;

public class FishingEvent implements Consumer<LogMonitorForMusicTask.Event> {
    private final RobotService robotService;

    private boolean fishing = false; //因为进入钓鱼界面会触发一次FISHING_FINISH，所以加个变量判定是否是钓鱼后触发
    private int finished = 0;

    public FishingEvent() {
        robotService = new RobotService();
    }

    @Override
    public void accept(LogMonitorForMusicTask.Event event) {
        if (!Config.setting.isFishing())
            return;

        switch (event) {
            case FISHING_START -> {
                fishing = true;
                finished = 0;
            }
            case FISHING_BAIT -> {
                if (Config.setting.isFishingBait()) {
                    PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                    pause.setOnFinished(event1 -> {
                        robotService.clickKeyCodeF();
                    });
                    pause.play();
                }
            }
            case FISHING_FINISH -> {
                if (Config.setting.isFishingFinish() && fishing) {
                    if (finished == 0) {
                        finished += 1;
                    } else if (finished == 1) {
                        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                        pause.setOnFinished(event1 -> {
                            robotService.clickKeyCodeESC();
                        });
                        pause.play();
                    }
                    fishing = false;
                }
            }
        }
    }

}
