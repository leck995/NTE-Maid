package cn.tealc.ntemaid.player;

import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.service.system.ConfigService;
import cn.tealc.ntemaid.service.system.player.PlayingListService;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MusicPlayerClient {
    private static final Logger log = LoggerFactory.getLogger(MusicPlayerClient.class);
    private static volatile MusicPlayerClient client;
    private final BaseAudioPlayer player;
    private static final String CONFIG_KEY = "last_music";
    private final ConfigService configService = AppInjector.getInstance(ConfigService.class);

    private MusicPlayerClient() {
        player = new FxMediaPlayer();
    }

    public void init() {
        initPlayList();
        initPlayingMusic();
        addMusicListChangListener();
    }

    /**
     * 添加播放列表监听，用于及时更新数据表
     *
     * @author leck
     * @date 2026/05/08
     */
    private void addMusicListChangListener() {
        PlayingListService playingListService = AppInjector.getInstance(PlayingListService.class);
        player.musics.addListener((ListChangeListener<? super Music>) change -> {
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
        PlayingListService service = AppInjector.getInstance(PlayingListService.class);
        List<Music> playinglist = service.getSavedPlayingList();
        if (!playinglist.isEmpty()) {
            player.init(playinglist, 0);
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


    /**
     * 保存当前播放记录，用于下次启动继续播放
     *
     * @author leck
     * @date 2026/05/09
     */
    public void close() {
        Music music = player.getPlayingMusic();
        if (music != null) {
            String id = String.valueOf(music.getId());
            String currentTime = String.valueOf(player.getCurrentTime());
            configService.setConfig("last_music", id, currentTime);
        } else {
            configService.removeConfig("last_music");
        }
    }
}
