package cn.tealc.ntemaid.player;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.thread.game.log.LogMonitorForMusicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
                case Off_VEHICLE, BEGIN_TRANSFER -> player.pauseWithFadeOut();
                case ON_VEHICLE -> player.playWithFadeIn();
            }
        });
        service.start();
    }


    public void init(){
        initPlayList();
        initLogMonitor();
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
            player.init(playlist,0);
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
}
