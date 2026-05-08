package cn.tealc.ntemaid.player;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.service.ConfigService;
import cn.tealc.ntemaid.service.PlayingListService;
import cn.tealc.ntemaid.service.impl.ConfigServiceImpl;
import cn.tealc.ntemaid.thread.game.log.LogMonitorForMusicService;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MusicPlayerClient {
    private static final Logger log = LoggerFactory.getLogger(MusicPlayerClient.class);
    private static volatile MusicPlayerClient client;
    private BaseAudioPlayer player;
    private static final String ON_VEHICLE = "End Get On Vehicle";
    private static final String Off_VEHICLE = "Start Get Off Vehicle";
    private static final String MUSIC_PLAYING = "UHTSoundSubsystem UHTUI_Vehicle::OnPlayOrPauseBtnCallBack ScrollMusicTitle.isValid = [1], bChecked = [1]";
    private static final String MUSIC_PAUSE = "UHTSoundSubsystem UHTUI_Vehicle::OnPlayOrPauseBtnCallBack ScrollMusicTitle.isValid = [1], bChecked = [0]";
    private static final String BEGIN_TRANSFER = "LevelTransferState BeginTransfer";


    private static final String CONFIG_KEY = "last_music";
    private ConfigService configService = new ConfigServiceImpl();

    private MusicPlayerClient() {
        player = new FxMediaPlayer();
    }

    private void initLogMonitor() {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path logPath = Paths.get(localAppData, "HT", "Saved", "Logs", "HT.log");
        LogMonitorForMusicService service = new LogMonitorForMusicService(logPath);
        service.setOnEventDetected(event -> {
            if (!Config.setting.isMusicEnable()){
                return;
            }
            switch (event) {
                case Off_VEHICLE, BEGIN_TRANSFER,ENDPLAY_RACING -> player.pauseWithFadeOut();
                case ON_VEHICLE -> player.playWithFadeIn();
            }
        });
        service.start();
    }


    public void init(){
        initPlayList();
        initPlayingMusic();
        initLogMonitor();
        addMusicListChangListener();
    }

    /**
     * 添加播放列表监听，用于及时更新数据表
     *
     * @author leck
     * @date 2026/05/08
     */
    private void addMusicListChangListener() {
        PlayingListService playingListService = new PlayingListService();
        player.musics.addListener((ListChangeListener<? super Music>) change ->{
            @SuppressWarnings("unchecked")
            List<Music> musicList = (List<Music>) change.getList();
            playingListService.updatePlayingListAsync(musicList);
        });
    }

    private void initPlayingMusic() {
        configService.getPairConfig(CONFIG_KEY).ifPresent(v -> {
            ObservableList<Music> musics = player.getMusics();
            if (musics.isEmpty()) return;
            int id = Integer.parseInt(v.getKey());
            int index = -1;
            for (int i = 0; i < musics.size(); i++) {
                if (musics.get(i).getId().equals(id)) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                double seconds = 0;
                try {
                    seconds = Double.parseDouble(v.getValue());
                } catch (NumberFormatException e) {
                    log.error("历史进度解析失败: {}", v.getValue());
                }
                Duration duration = Duration.seconds(seconds);
                player.play(index, false, duration);
            } else {
                log.info("列表中未找到历史歌曲 id: {}", v.getKey());
            }
        });
    }

    private void initPlayList() {
        PlayingListService service = new PlayingListService();
        List<Music> playinglist = service.getSavedPlayingList();
        if (!playinglist.isEmpty()){
            player.init(playinglist,5);
        }
    }


    public static MusicPlayerClient getInstance() {
        if (client == null) {
            synchronized (MusicPlayerClient.class) {
                if (client == null) {
                    client = new MusicPlayerClient();
                }
            }
        }
        return client;
    }

    public BaseAudioPlayer getPlayer() {
        return player;
    }


    public void save(){
        Music music = player.getPlayingMusic();
        if (music != null){
            String id = String.valueOf(music.getId());
            String currentTime = String.valueOf(player.getCurrentTime());
            configService.setConfig("last_music",id,currentTime);
        }else {
            configService.removeConfig("last_music");
        }
    }
}
