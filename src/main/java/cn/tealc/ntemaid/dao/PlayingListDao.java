package cn.tealc.ntemaid.dao;


import cn.tealc.ntemaid.model.game.GameTime;
import cn.tealc.ntemaid.model.game.music.Music;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class PlayingListDao {

    private static final Logger log = LoggerFactory.getLogger(PlayingListDao.class);
    private final QueryRunner qr = new QueryRunner();


    /**
     * 向列表末尾添加一首歌
     * @param musicId 歌曲ID
     * @param nextOrder 下一个排序序号（通常是当前列表 size）
     */
    public boolean addMusic(int musicId, int nextOrder) {
        String sql = "INSERT OR IGNORE INTO playing_list (music_id, sort_order) VALUES (?, ?)";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.update(conn, sql, musicId, nextOrder) > 0;
        } catch (SQLException e) {
            log.error("添加歌曲到播放队列失败", e);
            return false;
        }
    }

    /**
     * 从播放队列中移除指定歌曲
     */
    public boolean removeMusic(int musicId) {
        String sql = "DELETE FROM playing_list WHERE music_id = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.update(conn, sql, musicId) > 0;
        } catch (SQLException e) {
            log.error("从播放队列删除歌曲失败", e);
            return false;
        }
    }

    /**
     * 清空当前播放列表并存入新列表
     */
    public void savePlayingList(List<Music> list) {
        String deleteSql = "DELETE FROM playing_list";
        String insertSql = "INSERT INTO playing_list (music_id, sort_order) VALUES (?, ?)";
        
        try (Connection conn = JdbcUtils.getConnection()) {
            conn.setAutoCommit(false);
            try {
                qr.update(conn, deleteSql); // 先清空
                Object[][] params = new Object[list.size()][2];
                for (int i = 0; i < list.size(); i++) {
                    params[i][0] = list.get(i).getId();
                    params[i][1] = i;
                }
                qr.batch(conn, insertSql, params); // 批量插入新顺序
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空播放列表表数据
     */
    public boolean clear() {
        String sql = "DELETE FROM playing_list";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.update(conn, sql) >= 0;
        } catch (SQLException e) {
            log.error("清空播放列表失败", e);
            return false;
        }
    }
    /**
     * 获取持久化的播放列表详情
     */
    public List<Music> getPlayingList() {
        String sql = """
            SELECT m.* FROM music m
            JOIN playing_list p ON m.id = p.music_id
            ORDER BY p.sort_order ASC
            """;
        // 此处 RowProcessor 参考之前 MusicDao 的实现
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.query(conn, sql, new BeanListHandler<>(Music.class, MusicDao.getRowProcessor()));
        } catch (SQLException e) {
            return java.util.Collections.emptyList();
        }
    }
}