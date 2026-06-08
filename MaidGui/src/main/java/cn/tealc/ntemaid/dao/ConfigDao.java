package cn.tealc.ntemaid.dao;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;


public class ConfigDao {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigDao.class);
    private final QueryRunner qr = new QueryRunner();

    /**
     * 获取配置值
     */
    public Optional<String> getValue(String key) {
        String sql = "SELECT value FROM config WHERE key = ?";
        try (Connection con = JdbcUtils.getConnection()) {
            String result = qr.query(con, sql, new ScalarHandler<>(), key);
            return Optional.ofNullable(result);
        } catch (SQLException e) {
            LOG.error("获取配置值 {} 失败", key, e);
            return Optional.empty();
        }
    }

    /**
     * 保存或更新 (Upsert)
     */
    public void saveOrUpdate(String key, String value) {
        String sql = "INSERT INTO config (key, value) VALUES (?, ?) " +
                "ON CONFLICT(key) DO UPDATE SET value = excluded.value";
        try (Connection con = JdbcUtils.getConnection()) {
            qr.update(con, sql, key, value);
        } catch (SQLException e) {
            LOG.error("保存配置 {} 失败", key, e);
        }
    }

    /**
     * 删除配置
     */
    public int delete(String key) {
        String sql = "DELETE FROM config WHERE key = ?";
        try (Connection con = JdbcUtils.getConnection()) {
            return qr.update(con, sql, key);
        } catch (SQLException e) {
            LOG.error("删除配置 {} 失败", key, e);
            return 0;
        }
    }
    /**
     * 批量保存配置
     * @param configMap 配置键值对
     */
    public void saveAll(Map<String, String> configMap) {
        String sql = "INSERT INTO config (key, value) VALUES (?, ?) " +
                "ON CONFLICT(key) DO UPDATE SET value = excluded.value";

        try (Connection con = JdbcUtils.getConnection()) {
            con.setAutoCommit(false); // 开启事务
            try {
                Object[][] params = new Object[configMap.size()][2];
                int i = 0;
                for (Map.Entry<String, String> entry : configMap.entrySet()) {
                    params[i][0] = entry.getKey();
                    params[i][1] = entry.getValue();
                    i++;
                }
                qr.batch(con, sql, params);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOG.error("批量保存配置失败", e);
            throw new RuntimeException("批量保存失败", e);
        }
    }
}