package cn.tealc.ntemaid.service;

import cn.tealc.ntemaid.dao.PlaylistDao;
import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.model.game.music.Playlist;
import com.google.inject.Inject;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlaylistService {
    private final PlaylistDao playlistDao;

    @Inject
    public PlaylistService(PlaylistDao playlistDao) {
        this.playlistDao = playlistDao;
    }

    /**
     * 创建新歌单
     */
    public boolean createNewPlaylist(String name, String description) {
        if (name == null || name.trim().isEmpty()) return false;
        return playlistDao.createPlaylist(name, description).isPresent();
    }

    /**
     * 将歌曲添加到指定歌单
     */
    public boolean addSongToPlaylist(int playlistId, int musicId) {
        // 这里可以增加校验：比如检查歌曲和歌单是否都存在
        return playlistDao.addMusicToPlaylist(playlistId, musicId);
    }

    /**
     * 批量添加歌曲到歌单
     * @param playlistId 目标歌单
     * @param musicList 歌曲实体列表
     */
    public int addSongsToPlaylist(int playlistId, List<Music> musicList) {
        if (musicList == null || musicList.isEmpty()) return 0;

        // 提取 ID 列表进行批量处理
        List<Integer> ids = musicList.stream()
                .map(Music::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return playlistDao.addMusicToPlaylistBatch(playlistId, ids);
    }

    /**
     * 获取完整歌单信息（包含歌单详情和歌曲列表）
     */
    public Optional<Playlist> getPlaylistDetail(int playlistId) {
        // 先找到歌单基本信息
        List<Playlist> all = playlistDao.getAllPlaylists();
        Optional<Playlist> playlistOpt = all.stream()
                .filter(p -> p.getId() == playlistId)
                .findFirst();

        // 如果存在，则查询其包含的歌曲并填充
        playlistOpt.ifPresent(p -> {
            List<Music> songs = playlistDao.getMusicInPlaylist(playlistId);
            p.setSongs(songs);
        });
        
        return playlistOpt;
    }

    /**
     * 获取所有歌单列表（不含内部歌曲，用于展示侧边栏等）
     */
    public List<Playlist> getPlaylistList() {
        return playlistDao.getAllPlaylists();
    }

    /**
     * 异步删除歌单
     */
    public boolean deletePlaylist(Playlist playlist) {
        return playlistDao.deletePlaylist(playlist.getId());
    }

    /**
     * 异步移除歌单中的歌曲
     */
    public boolean removeMusicFromPlaylist(int playlistId, int musicId) {
        return playlistDao.removeMusicFromPlaylist(playlistId, musicId);
    }

    /**
     * 更新歌单基本信息
     * 包含非空校验逻辑
     */
    public boolean updatePlaylist(Playlist playlist) {
        if (playlist == null || playlist.getId() == null) {
            return false;
        }
        if (playlist.getName() == null || playlist.getName().trim().isEmpty()) {
            return false;
        }
        return playlistDao.updatePlaylist(playlist);
    }

    /**
     * 重命名歌单
     * @param id 歌单ID
     * @param newName 新名称
     */
    public boolean renamePlaylist(int id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            return false;
        }
        return playlistDao.updatePlaylistName(id, newName.trim());
    }


}