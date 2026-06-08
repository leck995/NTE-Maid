package cn.tealc.ntemaid.dao;

import cn.tealc.ntemaid.model.game.music.Music;
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

public class MusicDao {
    private static final Logger LOG = LoggerFactory.getLogger(MusicDao.class);
    private final QueryRunner qr = new QueryRunner();

    public static RowProcessor getRowProcessor() {
        Map<String, String> map = new HashMap<>();
        map.put("file_path", "filePath");
        map.put("add_time", "addTime");
        return new BasicRowProcessor(new BeanProcessor(map));
    }

    public Optional<Integer> addMusic(Music music) {
        String sql = "INSERT OR IGNORE INTO music (title, artist, album, duration, file_path, add_time) VALUES (?,?,?,?,?,?)";
        try (Connection conn = JdbcUtils.getConnection()) {
            Number id = qr.insert(conn, sql, new ScalarHandler<>(),
                    music.getTitle(), music.getArtist(), music.getAlbum(),
                    music.getDuration(), music.getFilePath(), System.currentTimeMillis());
            return Optional.ofNullable(id).map(Number::intValue);
        } catch (SQLException e) {
            LOG.error("添加歌曲失败: {}", music.getTitle(), e);
            return Optional.empty();
        }
    }

    /**
     * 批量添加歌曲（优化核心）
     * @return 成功插入的行数（排除已存在的）
     */
    public int addMusicBatch(List<Music> musicList) {
        if (musicList == null || musicList.isEmpty()) return 0;

        String sql = "INSERT OR IGNORE INTO music (title, artist, album, duration, file_path, add_time) VALUES (?,?,?,?,?,?)";
        Object[][] params = new Object[musicList.size()][6];

        for (int i = 0; i < musicList.size(); i++) {
            Music m = musicList.get(i);
            params[i][0] = m.getTitle();
            params[i][1] = m.getArtist();
            params[i][2] = m.getAlbum();
            params[i][3] = m.getDuration();
            params[i][4] = m.getFilePath();
            params[i][5] = m.getAddTime();
        }

        try (Connection conn = JdbcUtils.getConnection()) {
            // 关键优化：关闭自动提交，开启显式事务
            conn.setAutoCommit(false);
            try {
                int[] results = qr.batch(conn, sql, params);
                conn.commit();

                // 统计真正插入成功的行数（结果为1代表插入，0代表被IGNORE）
                return (int) Arrays.stream(results).filter(r -> r > 0).count();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOG.error("批量插入歌曲数据库失败", e);
            return 0;
        }
    }

    public List<Music> getAllMusic() {
        String sql = "SELECT * FROM music";
        try (Connection conn = JdbcUtils.getConnection()) {
            List<Music> result = qr.query(conn, sql, new BeanListHandler<>(Music.class, getRowProcessor()));
            return result != null ? result : Collections.emptyList();
        } catch (SQLException e) {
            LOG.error("获取歌曲列表失败", e);
            return Collections.emptyList();
        }
    }



    public boolean deleteAllMusic() {
        try (Connection conn = JdbcUtils.getConnection()) {
            conn.setAutoCommit(false);
            try {
                qr.update(conn, "DELETE FROM music");
                // 重置 SQLite 的自增计数器
                qr.update(conn, "DELETE FROM sqlite_sequence WHERE name='music'");
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOG.error("完全清空曲库失败", e);
            return false;
        }
    }

    public boolean deleteMusic(int id) {
        String sql = "DELETE FROM music WHERE id = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            // 由于级联删除已开启，关联表数据会自动删除
            return qr.update(conn, sql, id) > 0;
        } catch (SQLException e) {
            LOG.error("删除歌曲失败", e);
            return false;
        }
    }
}