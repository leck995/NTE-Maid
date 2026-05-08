package cn.tealc.ntemaid.ui.game.music;

import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.model.game.music.Playlist;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import cn.tealc.ntemaid.service.PlaylistService;
import cn.tealc.teafx.utils.message.MessageInfo;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class PlayListViewModel implements ViewModel {
    private final PlaylistService playlistService;
    private final ObservableList<Playlist> playlists = FXCollections.observableArrayList();
    private final ObservableList<Music> musicList = FXCollections.observableArrayList();
    private final SimpleObjectProperty<Playlist> selectedPlayList = new SimpleObjectProperty<>();

    public PlayListViewModel() {
        playlistService = new PlaylistService();
        getAllPlaylist();
    }

    private void getAllPlaylist() {
        List<Playlist> playlistList = playlistService.getPlaylistList();
        playlists.setAll(playlistList);
    }

    /**
     * 加载选中歌单中的歌曲
     * @param playlist
     * @author leck
     * @date 2026/05/08
     */
    public void loadMusicListPlaylist(Playlist playlist) {
        selectedPlayList.set(playlist);
        refreshMusicListInPlaylist();
    }

    /**
     * 刷新当前歌单的歌曲
     *
     * @author leck
     * @date 2026/05/08
     */
    public void refreshMusicListInPlaylist() {
        if (selectedPlayList.get()!=null){
            Optional<Playlist> detail = playlistService.getPlaylistDetail(selectedPlayList.get().getId());
            detail.ifPresent(d -> musicList.setAll(d.getSongs()));
        }
    }

    /**
     * 添加歌曲到指定歌单
     * @param music
     * @author leck
     * @date 2026/05/08
     */
    public void addToPlayingList(Music music) {
        MusicPlayerClient.getInstance().getPlayer().add(music);
    }

    /**
     * 播放制定歌曲
     * @param music
     * @author leck
     * @date 2026/05/08
     */
    public void playSelectedMusic(Music music) {
        MusicPlayerClient.getInstance().getPlayer().addAndPlay(music);
    }


    /**
     * 从歌单中移除歌曲
     * @param music
     * @author leck
     * @date 2026/05/08
     */
    public void deleteMusicFromPlayList(Music music) {
        boolean removed = playlistService.removeMusicFromPlaylist(selectedPlayList.get().getId(), music.getId());
        if (removed) {
            NotificationManager.message(MessageInfo.success("成功移除歌曲"));
            musicList.remove(music);
        } else {
            NotificationManager.message(MessageInfo.error("无法移除歌曲"));
        }
    }

    /**
     * 获取所有的歌单列表
     *
     * @return {@link List }<{@link Playlist }>
     * @author leck
     * @date 2026/05/08
     */
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

    /**
     * 创建歌单
     * @param name
     * @author leck
     * @date 2026/05/08
     */
    public void createPlaylist(String name) {
        boolean newPlaylist = playlistService.createNewPlaylist(name, null);
        if (newPlaylist) {
            NotificationManager.message(MessageInfo.success("成功创建歌单"));
            getAllPlaylist();
        } else {
            NotificationManager.message(MessageInfo.error("无法创建歌单"));
        }
    }

    /**
     * 播放当前歌单所有歌曲
     *
     * @author leck
     * @date 2026/05/08
     */
    public void playSelectedPlaylist() {
        if (!musicList.isEmpty()){
            MusicPlayerClient.getInstance().getPlayer().initAndPlay(musicList,0);
            NotificationManager.message(MessageInfo.success("即将开始播放"));
        }
    }


    /**
     * 删除指定歌单
     * @param playlist
     * @author leck
     * @date 2026/05/08
     */
    public void deletePlaylist(Playlist playlist) {
        boolean deleted = playlistService.deletePlaylist(playlist);
        if (deleted) {
            if (playlist == selectedPlayList.get()){
                musicList.clear();
                selectedPlayList.set(null);
            }
            getAllPlaylist();
            NotificationManager.message(MessageInfo.success("成功删除歌单"));
        } else {
            NotificationManager.message(MessageInfo.error("无法删除歌单"));
        }
    }

    /**
     * 修改歌单名称
     * @param playlist
     * @param newName
     * @author leck
     * @date 2026/05/08
     */
    public void updatePlaylist(Playlist playlist,String newName) {
        boolean update = playlistService.renamePlaylist(playlist.getId(),newName);
        if (update) {
            NotificationManager.message(MessageInfo.success("成功修改歌单"));
            getAllPlaylist();
        } else {
            NotificationManager.message(MessageInfo.error("无法修改歌单"));
        }
    }

    public ObservableList<Playlist> getPlaylists() {
        return playlists;
    }

    public ObservableList<Music> getMusicList() {
        return musicList;
    }
}
