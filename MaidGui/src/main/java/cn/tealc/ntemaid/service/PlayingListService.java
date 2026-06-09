package cn.tealc.ntemaid.service;

import cn.tealc.ntemaid.dao.PlayingListDao;
import cn.tealc.ntemaid.model.game.music.Music;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayingListService {
    private static final Logger LOG = LoggerFactory.getLogger(PlayingListService.class);
    private final PlayingListDao playingListDao;

    @Inject
    public PlayingListService(PlayingListDao playingListDao) {
        this.playingListDao = playingListDao;
    }

    /**
     * 异步添加单首歌曲到队列
     */
    public void addMusicAsync(Music music, int currentListSize) {
        if (music == null || music.getId() == null) return;
        CompletableFuture.runAsync(() -> {
            playingListDao.addMusic(music.getId(), currentListSize);
        });
    }

    /**
     * 异步从队列删除单首歌曲
     */
    public void removeMusicAsync(Music music) {
        if (music == null || music.getId() == null) return;
        CompletableFuture.runAsync(() -> {
            playingListDao.removeMusic(music.getId());
        });
    }

    /**
     * 获取上次保存的播放列表
     */
    public List<Music> getSavedPlayingList() {
        return playingListDao.getPlayingList();
    }

    /**
     * 异步更新播放列表（防止阻塞 UI 线程）
     * 每当用户清空列表、添加歌曲或重新排序时调用
     */
    public void updatePlayingListAsync(List<Music> currentList) {
        // 使用 CompletableFuture 在后台线程保存，不影响音乐播放
        CompletableFuture.runAsync(() -> {
            try {
                playingListDao.savePlayingList(currentList);
                LOG.debug("播放列表已同步至数据库，共 {} 首", currentList.size());
            } catch (Exception e) {
                LOG.error("同步播放列表失败", e);
            }
        });
    }

    /**
     * 显式清空播放列表（异步）
     */
    public void clearPlayingListAsync() {
        CompletableFuture.runAsync(() -> {
            boolean success = playingListDao.clear();
            if (success) {
                LOG.debug("数据库播放列表清空成功");
            }
        });
    }

    /**
     * 记录当前播放到的歌曲索引（存入 config 表）
     * @param index 歌曲在列表中的位置
     */
    public void saveCurrentIndex(int index) {
        // 假设你有一个 ConfigDao 或者直接操作 config 表
        // SQL: INSERT OR REPLACE INTO config (key, value) VALUES ('last_play_index', ?)
    }
}