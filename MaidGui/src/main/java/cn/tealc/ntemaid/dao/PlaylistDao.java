package cn.tealc.ntemaid.dao;


import cn.tealc.ntemaid.model.game.music.Music;
import cn.tealc.ntemaid.model.game.music.Playlist;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class PlaylistDao {
    private static final Logger LOG = LoggerFactory.getLogger(PlaylistDao.class);
    private final QueryRunner qr = new QueryRunner();

    private RowProcessor getPlaylistProcessor() {
        Map<String, String> map = new HashMap<>();
        map.put("cover_path", "coverPath");
        map.put("create_time", "createTime");
        return new BasicRowProcessor(new BeanProcessor(map));
    }

    /**
     * 创建新歌单
     */
    public Optional<Integer> createPlaylist(String name, String desc) {
        String sql = "INSERT INTO playlist (name, description, create_time) VALUES (?,?,?)";
        try (Connection conn = JdbcUtils.getConnection()) {
            Number id = qr.insert(conn, sql, new ScalarHandler<>(), name, desc, System.currentTimeMillis());
            return Optional.ofNullable(id).map(Number::intValue);
        } catch (SQLException e) {
            LOG.error("创建歌单失败: {}", name, e);
            return Optional.empty();
        }
    }

    /**
     * 向歌单添加歌曲
     */
    public boolean addMusicToPlaylist(int playlistId, int musicId) {
        String sql = "INSERT OR IGNORE INTO playlist_music_relation (playlist_id, music_id) VALUES (?,?)";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.update(conn, sql, playlistId, musicId) > 0;
        } catch (SQLException e) {
            LOG.error("歌曲添加至歌单失败: pId={}, mId={}", playlistId, musicId, e);
            return false;
        }
    }

    /**
     * 批量向歌单添加歌曲 (优化核心)
     * @param playlistId 歌单ID
     * @param musicIds 歌曲ID列表
     */
    public int addMusicToPlaylistBatch(int playlistId, List<Integer> musicIds) {
        if (musicIds == null || musicIds.isEmpty()) return 0;

        String sql = "INSERT OR IGNORE INTO playlist_music_relation (playlist_id, music_id) VALUES (?,?)";
        Object[][] params = new Object[musicIds.size()][2];
        for (int i = 0; i < musicIds.size(); i++) {
            params[i][0] = playlistId;
            params[i][1] = musicIds.get(i);
        }

        try (Connection conn = JdbcUtils.getConnection()) {
            conn.setAutoCommit(false); // 开启事务
            try {
                int[] results = qr.batch(conn, sql, params);
                conn.commit();
                return (int) Arrays.stream(results).filter(r -> r > 0).count();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOG.error("批量添加歌曲到歌单失败: pId={}", playlistId, e);
            return 0;
        }
    }
    /**
     * 获取指定歌单的所有歌曲
     */
    public List<Music> getMusicInPlaylist(int playlistId) {
        String sql = """
            SELECT m.* FROM music m 
            JOIN playlist_music_relation pmr ON m.id = pmr.music_id 
            WHERE pmr.playlist_id = ?
            """;
        // 注意：这里需要映射 Music 的 RowProcessor
        Map<String, String> map = new HashMap<>();
        map.put("file_path", "filePath");
        map.put("add_time", "addTime");
        RowProcessor musicProcessor = new BasicRowProcessor(new BeanProcessor(map));

        try (Connection conn = JdbcUtils.getConnection()) {
            List<Music> result = qr.query(conn, sql, new BeanListHandler<>(Music.class, musicProcessor), playlistId);
            return result != null ? result : Collections.emptyList();
        } catch (SQLException e) {
            LOG.error("获取歌单内歌曲失败: id={}", playlistId, e);
            return Collections.emptyList();
        }
    }

    public List<Playlist> getAllPlaylists() {
        String sql = "SELECT * FROM playlist";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.query(conn, sql, new BeanListHandler<>(Playlist.class, getPlaylistProcessor()));
        } catch (SQLException e) {
            LOG.error("获取歌单列表失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 删除整个歌单
     * 注意：由于开启了级联删除(CASCADE)，playlist_music_relation 中对应的记录会自动删除
     */
    public boolean deletePlaylist(int playlistId) {
        String sql = "DELETE FROM playlist WHERE id = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.update(conn, sql, playlistId) > 0;
        } catch (SQLException e) {
            LOG.error("删除歌单失败: id={}", playlistId, e);
            return false;
        }
    }

    /**
     * 从指定歌单中移除一首歌
     * 仅删除关联，不影响 music 表中的歌曲文件
     */
    public boolean removeMusicFromPlaylist(int playlistId, int musicId) {
        String sql = "DELETE FROM playlist_music_relation WHERE playlist_id = ? AND music_id = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.update(conn, sql, playlistId, musicId) > 0;
        } catch (SQLException e) {
            LOG.error("从歌单中移除歌曲失败: pId={}, mId={}", playlistId, musicId, e);
            return false;
        }
    }

    /**
     * 清空歌单内的所有歌曲
     */
    public boolean clearMusicInPlaylist(int playlistId) {
        String sql = "DELETE FROM playlist_music_relation WHERE playlist_id = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.update(conn, sql, playlistId) >= 0;
        } catch (SQLException e) {
            LOG.error("清空歌单歌曲失败: id={}", playlistId, e);
            return false;
        }
    }

    /**
     * 更新歌单基本信息 (名称、描述、封面路径)
     * @param playlist 包含更新后数据的歌单对象
     * @return 是否修改成功
     */
    public boolean updatePlaylist(Playlist playlist) {
        String sql = "UPDATE playlist SET name = ?, description = ?, cover_path = ? WHERE id = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            int rows = qr.update(conn, sql,
                    playlist.getName(),
                    playlist.getDescription(),
                    playlist.getCoverPath(),
                    playlist.getId());
            return rows > 0;
        } catch (SQLException e) {
            LOG.error("修改歌单信息失败: id={}", playlist.getId(), e);
            return false;
        }
    }

    /**
     * 仅修改歌单名称
     * @param id 歌单ID
     * @param newName 新名称
     * @return 是否修改成功
     */
    public boolean updatePlaylistName(int id, String newName) {
        String sql = "UPDATE playlist SET name = ? WHERE id = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.update(conn, sql, newName, id) > 0;
        } catch (SQLException e) {
            LOG.error("修改歌单名称失败: id={}", id, e);
            return false;
        }
    }
}