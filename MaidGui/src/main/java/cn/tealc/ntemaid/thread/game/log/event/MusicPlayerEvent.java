package cn.tealc.ntemaid.thread.game.log.event;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.player.BaseAudioPlayer;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import cn.tealc.ntemaid.service.RobotService;
import cn.tealc.ntemaid.thread.game.log.LogMonitorForMusicTask;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.function.Consumer;

public class MusicPlayerEvent implements Consumer<LogMonitorForMusicTask.Event> {

    private final BaseAudioPlayer player;

    public MusicPlayerEvent() {
        player = MusicPlayerClient.getInstance().getPlayer();
    }

    @Override
    public void accept(LogMonitorForMusicTask.Event event) {
        if (!Config.setting.isMusicEnable())
            return;
        switch (event) {
            case Off_VEHICLE, BEGIN_TRANSFER, ENDPLAY_RACING -> player.pauseWithFadeOut();
            case ON_VEHICLE -> player.playWithFadeIn();
            case MUSIC_PLAYING -> {
                stopGameFirstMusic();
            }
        }
    }

    /**
     * 关闭游戏内音乐
     *
     * @author leck
     * @date 2026/05/09
     */
    public void stopGameFirstMusic() {
        PauseTransition pause = new PauseTransition(Duration.millis(1500));
        pause.setOnFinished(e -> {
            RobotService robotService = new RobotService();
            robotService.clickKeyCodeDigit2();
        });
        pause.play();

    }
}
