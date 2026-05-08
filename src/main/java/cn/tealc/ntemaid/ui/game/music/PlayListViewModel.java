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
    private ObservableList<Playlist> playlists = FXCollections.observableArrayList();
    private ObservableList<Music> musicList = FXCollections.observableArrayList();
    private SimpleObjectProperty<Playlist> selectedPlayList = new SimpleObjectProperty<>();

    public PlayListViewModel() {
        playlistService = new PlaylistService();
        List<Playlist> playlistList = playlistService.getPlaylistList();
        playlists.setAll(playlistList);
    }

    public void loadPlaylist(Playlist playlist) {
        selectedPlayList.set(playlist);
        Optional<Playlist> detail = playlistService.getPlaylistDetail(playlist.getId());
        detail.ifPresent(d -> musicList.setAll(d.getSongs()));
    }



    public ObservableList<Playlist> getPlaylists() {
        return playlists;
    }

    public ObservableList<Music> getMusicList() {
        return musicList;
    }

    public void addToPlayingList(Music music) {
        MusicPlayerClient.getInstance().getPlayer().add(music);
    }

    public void playSelectedMusic(Music music) {
        MusicPlayerClient.getInstance().getPlayer().addAndPlay(music);
    }



    public void deleteMusicFromPlayList(Music music) {
        boolean removed = playlistService.removeMusicFromPlaylist(selectedPlayList.get().getId(), music.getId());
        if (removed) {
            NotificationManager.message(MessageInfo.success("成功移除歌曲"));
            musicList.remove(music);
        } else {
            NotificationManager.message(MessageInfo.error("无法移除歌曲"));
        }

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

}
