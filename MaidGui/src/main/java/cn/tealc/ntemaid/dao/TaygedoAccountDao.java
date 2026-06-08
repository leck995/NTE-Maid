package cn.tealc.ntemaid.dao;

import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 塔吉多账号数据访问层
 * 操作 taygedo_account 表，使用 commons-dbutils BeanHandler
 */
public class TaygedoAccountDao {
    private static final Logger LOG = LoggerFactory.getLogger(TaygedoAccountDao.class);
    private final QueryRunner qr = new QueryRunner();

    /**
     * 数据库字段 → Java属性名映射（snake_case → camelCase）
     */
    private RowProcessor getRowProcessor() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("device_id", "deviceId");
        mapping.put("laohu_token", "laohuToken");
        mapping.put("laohu_user_id", "laohuUserId");
        mapping.put("access_token", "accessToken");
        mapping.put("refresh_token", "refreshToken");
        mapping.put("role_id", "roleId");
        mapping.put("role_name", "roleName");
        mapping.put("token_updated_at", "tokenUpdatedAt");
        mapping.put("created_at", "createdAt");
        mapping.put("updated_at", "updatedAt");
        return new BasicRowProcessor(new BeanProcessor(mapping));
    }

    /**
     * 保存或更新账号（以 phone 为主键）
     */
    public void saveOrUpdate(TaygedoAccount account) {
        String sql = """
            INSERT INTO taygedo_account (
                phone, name, device_id, openudid, vendorid,
                laohu_token, laohu_user_id, access_token, refresh_token, uid,
                role_id, role_name, token_updated_at, created_at, updated_at
            ) VALUES (
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?
            ) ON CONFLICT(phone) DO UPDATE SET
                name = excluded.name,
                device_id = excluded.device_id,
                openudid = excluded.openudid,
                vendorid = excluded.vendorid,
                laohu_token = excluded.laohu_token,
                laohu_user_id = excluded.laohu_user_id,
                access_token = excluded.access_token,
                refresh_token = excluded.refresh_token,
                uid = excluded.uid,
                role_id = excluded.role_id,
                role_name = excluded.role_name,
                token_updated_at = excluded.token_updated_at,
                updated_at = excluded.updated_at
            """;

        long now = System.currentTimeMillis();
        try (Connection conn = JdbcUtils.getConnection()) {
            qr.update(conn, sql,
                    account.getPhone(),
                    account.getName(),
                    account.getDeviceId(),
                    account.getOpenudid(),
                    account.getVendorid(),
                    account.getLaohuToken(),
                    account.getLaohuUserId(),
                    account.getAccessToken(),
                    account.getRefreshToken(),
                    account.getUid(),
                    account.getRoleId(),
                    account.getRoleName(),
                    account.getTokenUpdatedAt(),
                    now,   // created_at（新插入时使用）
                    now    // updated_at
            );
            LOG.info("账号 {} 已保存到数据库", account.getPhone());
        } catch (SQLException e) {
            LOG.error("保存账号 {} 失败", account.getPhone(), e);
        }
    }

    /**
     * 根据手机号查询账号
     */
    public TaygedoAccount findByPhone(String phone) {
        String sql = "SELECT * FROM taygedo_account WHERE phone = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.query(conn, sql, new BeanHandler<>(TaygedoAccount.class, getRowProcessor()), phone);
        } catch (SQLException e) {
            LOG.error("查询账号 {} 失败", phone, e);
            return null;
        }
    }

    /**
     * 获取第一个账号（单账号场景下最便捷的查询方式）
     */
    public TaygedoAccount findFirst() {
        String sql = "SELECT * FROM taygedo_account LIMIT 1";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.query(conn, sql, new BeanHandler<>(TaygedoAccount.class, getRowProcessor()));
        } catch (SQLException e) {
            LOG.error("查询账号失败", e);
            return null;
        }
    }

    /**
     * 获取所有账号
     */
    public List<TaygedoAccount> findAll() {
        String sql = "SELECT * FROM taygedo_account ORDER BY updated_at DESC";
        try (Connection conn = JdbcUtils.getConnection()) {
            List<TaygedoAccount> result = qr.query(conn, sql,
                    new BeanListHandler<>(TaygedoAccount.class, getRowProcessor()));
            return result != null ? result : Collections.emptyList();
        } catch (SQLException e) {
            LOG.error("查询所有账号失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 删除账号
     */
    public void delete(String phone) {
        String sql = "DELETE FROM taygedo_account WHERE phone = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            qr.update(conn, sql, phone);
            LOG.info("账号 {} 已删除", phone);
        } catch (SQLException e) {
            LOG.error("删除账号 {} 失败", phone, e);
        }
    }
}
