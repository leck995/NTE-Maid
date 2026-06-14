package cn.tealc.ntemaid.dao;

import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaItem;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonGachaDao {
    private final QueryRunner qr = new QueryRunner();

    private RowProcessor getRowProcessor() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("player_id", "playerId");
        mapping.put("record_id", "recordId");
        mapping.put("record_type", "recordType");
        mapping.put("pool_id", "poolId");
        mapping.put("pool_name", "poolName");
        mapping.put("item_id", "itemId");
        mapping.put("item_name", "itemName");
        mapping.put("roll_points", "rollPoints");
        mapping.put("roll_label", "rollLabel");
        return new BasicRowProcessor(new BeanProcessor(mapping));
    }

    public long save(CommonGachaItem item) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO game_common_gacha (player_id, record_id, record_type, time, pool_id, pool_name,
                item_id, item_name, count, roll_points, roll_label, sort)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.insert(conn, sql, new ScalarHandler<>(),
                    item.getPlayerId(), item.getRecordId(), item.getRecordType(),
                    item.getTime(), item.getPoolId(), item.getPoolName(),
                    item.getItemId(), item.getItemName(), item.getCount(),
                    item.getRollPoints(), item.getRollLabel(), item.getSort());
        }
    }

    public void saveAll(List<CommonGachaItem> list) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO game_common_gacha (player_id, record_id, record_type, time, pool_id, pool_name,
                item_id, item_name, count, roll_points, roll_label, sort)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = JdbcUtils.getConnection()) {
            Object[][] params = new Object[list.size()][];
            for (int i = 0; i < list.size(); i++) {
                CommonGachaItem d = list.get(i);
                params[i] = new Object[]{
                        d.getPlayerId(), d.getRecordId(), d.getRecordType(),
                        d.getTime(), d.getPoolId(), d.getPoolName(),
                        d.getItemId(), d.getItemName(), d.getCount(),
                        d.getRollPoints(), d.getRollLabel(), d.getSort()
                };
            }
            qr.batch(conn, sql, params);
        }
    }

    /**
     * 按时间倒序、sort 升序获取指定 playerId 的所有记录
     */
    public List<CommonGachaItem> findByPlayerIdOrderByTimeDescSortAsc(String playerId) throws SQLException {
        String sql = "SELECT * FROM game_common_gacha WHERE player_id = ? ORDER BY time DESC, sort ASC";
        try (Connection conn = JdbcUtils.getConnection()) {
            List<CommonGachaItem> result = qr.query(conn, sql,
                    new BeanListHandler<>(CommonGachaItem.class, getRowProcessor()), playerId);
            return result != null ? result : Collections.emptyList();
        }
    }

    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM game_common_gacha";
        try (Connection conn = JdbcUtils.getConnection()) {
            Number result = qr.query(conn, sql, new ScalarHandler<>());
            return result != null ? result.longValue() : 0L;
        }
    }

    public List<String> findDistinctPlayerIds() throws SQLException {
        String sql = "SELECT DISTINCT player_id FROM game_common_gacha ORDER BY player_id";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.query(conn, sql, rs -> {
                List<String> ids = new ArrayList<>();
                while (rs.next()) ids.add(rs.getString(1));
                return ids;
            });
        }
    }

    public int deleteByPlayerId(String playerId) throws SQLException {
        String sql = "DELETE FROM game_common_gacha WHERE player_id = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.update(conn, sql, playerId);
        }
    }
}
