package cn.tealc.ntemaid.player;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.service.ConfigService;
import cn.tealc.ntemaid.service.impl.ConfigServiceImpl;
import cn.tealc.ntemaid.thread.game.log.LogMonitorForMusicService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
    }

    private void initPlayingMusic() {
        configService.getPairConfig(CONFIG_KEY).ifPresent(v -> {
            ObservableList<Music> musics = player.getMusics();
            if (musics.isEmpty()) return;
            String historyUrl = v.getKey();
            int index = -1;
            for (int i = 0; i < musics.size(); i++) {
                if (musics.get(i).getUrl().equals(historyUrl)) {
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
                log.info("列表中未找到历史歌曲 URL: {}", historyUrl);
            }
        });
    }

    private void initPlayList() {
        String musicDir = Config.setting.getMusicDir();
        if (musicDir == null)
            return;
        Path rootPath = Paths.get(musicDir);
        if (!Files.exists(rootPath)) {
            log.info("歌曲目录不存在，跳过初始化歌单");
            return;
        }
        try (Stream<Path> stream = Files.walk(rootPath)) {
            List<Music> playlist = stream
                    .filter(Files::isRegularFile) // 只处理文件
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".mp3") || name.endsWith(".wav");
                    })
                    .map(path -> {
                        // 3. 映射为 Music 对象
                        String absolutePath = path.toAbsolutePath().toString();
                        String fileName = path.getFileName().toString();
                        return new Music(absolutePath, fileName);
                    })
                    .toList();
            player.init(playlist,5);
        } catch (IOException e) {
            log.debug("初始化歌单错误，｛｝",e);
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
        String url = music.getUrl();
        double currentTime = player.getCurrentTime();


        configService.setConfig("last_music",url,String.valueOf(currentTime));
    }
}
