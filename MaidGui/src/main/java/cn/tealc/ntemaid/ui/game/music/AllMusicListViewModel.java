package cn.tealc.ntemaid.ui.game.music;

import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.model.game.music.Playlist;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import cn.tealc.ntemaid.service.MusicService;
import cn.tealc.ntemaid.service.PlaylistService;
import cn.tealc.teafx.utils.message.MessageInfo;
import de.saxsys.mvvmfx.ViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class AllMusicListViewModel implements ViewModel {
    private static final Logger log = LoggerFactory.getLogger(AllMusicListViewModel.class);
    private final MusicService musicService;
    private final PlaylistService playlistService;
    private final ObservableList<Music> allMusicList;


    public AllMusicListViewModel() {
        musicService = new MusicService();
        playlistService = new PlaylistService();
        allMusicList = FXCollections.observableArrayList();
        refreshMusicList();
    }

    public void refreshMusicList() {
        List<Music> allMusic = musicService.getAllMusic();
        allMusicList.setAll(allMusic);
    }

    /**
     * 修改后的加载逻辑：使用 Service 进行异步扫描和入库
     */
    public void loadMusicListFromDir(File dir) {
        if (dir == null || !dir.exists()) return;
        Task<Integer> scanTask = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                return musicService.scanAndImportDirectory(dir.getAbsolutePath());
            }
        };

        scanTask.setOnSucceeded(event -> {
            int addedCount = scanTask.getValue();
            refreshMusicList();
            NotificationManager.message(MessageInfo.success(
                    String.format("同步完成：新增 %d 首，曲库共 %d 首歌曲", addedCount, allMusicList.size())));
        });

        scanTask.setOnFailed(event -> {
            Throwable e = scanTask.getException();
            log.error("加载音乐目录失败", e);
            NotificationManager.message(MessageInfo.error("加载音乐目录失败: " + e.getMessage()));
        });
        Thread.startVirtualThread(scanTask);
    }

    public void deleteMusicFromLibrary(Music music) {
        boolean deleted = musicService.deleteMusic(music.getId());
        if (deleted) {
            String format = String.format("删除歌曲 %s 成功 ", music.getTitle());
            NotificationManager.message(MessageInfo.success(format));
            MusicPlayerClient.getInstance().getPlayer().removeMusic(music);
            allMusicList.remove(music);
        } else {
            String format = String.format("删除歌曲 %s 失败 ", music.getTitle());
            NotificationManager.message(MessageInfo.error(format));
        }
    }

    public void deleteAllMusicFromLibrary() {
        boolean deleted = musicService.deleteAllMusic();
        if (deleted) {
            NotificationManager.message(MessageInfo.success("已清空曲库"));
            allMusicList.clear();
            MusicPlayerClient.getInstance().getPlayer().clearPlayingList();
        } else {
            NotificationManager.message(MessageInfo.error("无法清空曲库"));
        }

    }

    public void playAll() {
        MusicPlayerClient.getInstance().getPlayer().initAndPlay(allMusicList, 0);
    }


    public void addToPlayingList(Music music) {
        MusicPlayerClient.getInstance().getPlayer().add(music);
    }

    public void playSelectedMusic(Music music) {
        MusicPlayerClient.getInstance().getPlayer().addAndPlay(music);
    }

    public ObservableList<Music> getAllMusicList() {
        return allMusicList;
    }


    public List<Playlist> getAllPlaylists() {
        return playlistService.getPlaylistList();
    }

    public void addMusicToPlaylist(Music music, Playlist playlist) {
        boolean added = playlistService.addSongToPlaylist(playlist.getId(), music.getId());
        if (added) {
            NotificationManager.message(MessageInfo.success("成功添加到歌单"));
        } else {
            NotificationManager.message(MessageInfo.error("无法添加到歌单"));
        }
    }

    public void addMusicToPlaylist(List<Music> musicList, Playlist playlist) {
        if (musicList == null || musicList.isEmpty()) return;
        int added = playlistService.addSongsToPlaylist(playlist.getId(), musicList);
        if (added > 0) {
            NotificationManager.message(MessageInfo.success(
                    String.format("成功添加 %d 首到歌单「%s」", added, playlist.getName())));
        } else {
            NotificationManager.message(MessageInfo.error("无法添加到歌单"));
        }
    }

    public void deleteMusicFromLibrary(List<Music> musicList) {
        if (musicList == null || musicList.isEmpty()) return;

        List<Integer> ids = musicList.stream()
                .map(Music::getId)
                .collect(Collectors.toList());
        int deleted = musicService.deleteMusicBatch(ids);

        if (deleted > 0) {
            for (Music music : musicList) {
                MusicPlayerClient.getInstance().getPlayer().removeMusic(music);
            }
            allMusicList.removeAll(musicList);
            NotificationManager.message(MessageInfo.success(
                    String.format("成功删除 %d 首歌曲", deleted)));
        } else {
            NotificationManager.message(MessageInfo.error("删除歌曲失败"));
        }
    }

}
