package cn.tealc.ntemaid.dao;

import cn.tealc.ntemaid.model.game.gacha.LocalGachaData;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LocalGachaDataDao {
    private final QueryRunner qr = new QueryRunner();

    private RowProcessor getRowProcessor() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("role_id", "roleId");
        mapping.put("gacha_type", "gachaType");
        mapping.put("lucky_type", "luckyType");
        mapping.put("rare_count", "rareCount");
        mapping.put("time_stamp", "timeStamp");
        return new BasicRowProcessor(new BeanProcessor(mapping));
    }

    public long save(LocalGachaData data) throws SQLException {
        String sql = """
            INSERT INTO game_gacha (role_id, gacha_type, charid, lucky_type, rare_count, time, time_stamp)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.insert(conn, sql, new org.apache.commons.dbutils.handlers.ScalarHandler<>(),
                    data.getRoleId(),
                    data.getGachaType().getCode(),
                    data.getCharid(),
                    data.getLuckyType(),
                    data.getRareCount(),
                    data.getTime(),
                    data.getTimeStamp());
        }
    }

    public void saveAll(List<LocalGachaData> list) throws SQLException {
        String sql = """
            INSERT INTO game_gacha (role_id, gacha_type, charid, lucky_type, rare_count, time, time_stamp)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = JdbcUtils.getConnection()) {
            Object[][] params = new Object[list.size()][];
            for (int i = 0; i < list.size(); i++) {
                LocalGachaData d = list.get(i);
                params[i] = new Object[]{
                        d.getRoleId(), d.getGachaType().getCode(), d.getCharid(),
                        d.getLuckyType(), d.getRareCount(), d.getTime(), d.getTimeStamp()
                };
            }
            qr.batch(conn, sql, params);
        }
    }

    public Optional<LocalGachaData> findById(long id) throws SQLException {
        String sql = "SELECT * FROM game_gacha WHERE id = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            return Optional.ofNullable(
                    qr.query(conn, sql, new BeanHandler<>(LocalGachaData.class, getRowProcessor()), id));
        }
    }

    public List<LocalGachaData> findByRoleId(String roleId) throws SQLException {
        String sql = "SELECT * FROM game_gacha WHERE role_id = ? ORDER BY id DESC";
        try (Connection conn = JdbcUtils.getConnection()) {
            List<LocalGachaData> result = qr.query(conn, sql,
                    new BeanListHandler<>(LocalGachaData.class, getRowProcessor()), roleId);
            return result != null ? result : Collections.emptyList();
        }
    }

    public List<LocalGachaData> findAll() throws SQLException {
        String sql = "SELECT * FROM game_gacha ORDER BY id DESC";
        try (Connection conn = JdbcUtils.getConnection()) {
            List<LocalGachaData> result = qr.query(conn, sql,
                    new BeanListHandler<>(LocalGachaData.class, getRowProcessor()));
            return result != null ? result : Collections.emptyList();
        }
    }

    public boolean deleteById(long id) throws SQLException {
        String sql = "DELETE FROM game_gacha WHERE id = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.update(conn, sql, id) > 0;
        }
    }

    public int deleteByRoleId(String roleId) throws SQLException {
        String sql = "DELETE FROM game_gacha WHERE role_id = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.update(conn, sql, roleId);
        }
    }

    /**
     * 查询指定 roleId 最晚的 timeStamp，无记录时返回 0
     */
    public long findLatestTimeStampByRoleId(String roleId) throws SQLException {
        String sql = "SELECT COALESCE(MAX(time_stamp), 0) FROM game_gacha WHERE role_id = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            Number result = qr.query(conn, sql, new ScalarHandler<>(), roleId);
            return result != null ? result.longValue() : 0L;
        }
    }
}
