package cn.tealc.ntemaid.thread.game.log.event;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.jna.Win32KeySender;
import cn.tealc.ntemaid.player.BaseAudioPlayer;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import javafx.util.Duration;

import java.util.function.Consumer;

public class MusicPlayerEvent implements Consumer<String> {
    private static final String ON_VEHICLE = "End Get On Vehicle";
    private static final String Off_VEHICLE = "Start Get Off Vehicle";
    private static final String MUSIC_PLAYING = "UHTSoundSubsystem UHTUI_Vehicle::OnPlayOrPauseBtnCallBack ScrollMusicTitle.isValid = [1], bChecked = [1]";
    private static final String MUSIC_PAUSE = "UHTSoundSubsystem UHTUI_Vehicle::OnPlayOrPauseBtnCallBack ScrollMusicTitle.isValid = [1], bChecked = [0]";
    private static final String BEGIN_TRANSFER = "LevelTransferState BeginTransfer";
    private static final String ENDPLAY_RACING = "EndPlay_Racing LEVEL_TYPE_RACING_PVP";
    private static final String ONLINE_TEAM_JOIN = "JoinChannel ChatChannelID";
    private static final String ONLINE_TEAM_QUITE = "DebugChat:QuitTeamChatChannel";
    private final BaseAudioPlayer player;

    private boolean stopUserPlayer = false;


    public MusicPlayerEvent() {
        player = MusicPlayerClient.getInstance().getPlayer();
    }

    @Override
    public void accept(String row) {
        if (!Config.setting.isMusicEnable())
            return;
        if (row.contains(ONLINE_TEAM_JOIN)){ //如果联机，禁止播放
            stopUserPlayer = true;
        }else if (row.contains(ONLINE_TEAM_QUITE)){
            stopUserPlayer = false;
        }else if (row.contains(Off_VEHICLE) || row.contains(BEGIN_TRANSFER) || row.contains(ENDPLAY_RACING)){
            player.pauseWithFadeOut();
        }else if (row.contains(ON_VEHICLE)){
            if (!stopUserPlayer)
                player.playWithFadeIn();
        }else if (row.contains(MUSIC_PLAYING)){
            stopGameFirstMusic();
        }
    }

    /**
     * 关闭游戏内音乐
     *
     * @author leck
     * @date 2026/05/09
     */
    public void stopGameFirstMusic() {
        Win32KeySender win32KeySender = new Win32KeySender();
        win32KeySender.clickKey(Win32KeySender.VirtualKey.F,Duration.millis(1500));



    }
}
