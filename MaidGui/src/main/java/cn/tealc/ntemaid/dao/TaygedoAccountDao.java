package cn.tealc.ntemaid.dao;

import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 塔吉多账号数据访问层
 * 操作 taygedo_account 表，使用 commons-dbutils BeanHandler
 * 异常直接上抛，由调用方处理
 */
public class TaygedoAccountDao {
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
        mapping.put("server_id", "serverId");
        mapping.put("server_name", "serverName");
        mapping.put("game_id", "gameId");
        mapping.put("token_updated_at", "tokenUpdatedAt");
        mapping.put("created_at", "createdAt");
        mapping.put("updated_at", "updatedAt");
        return new BasicRowProcessor(new BeanProcessor(mapping));
    }

    /**
     * 保存或更新账号（以 phone 为主键）
     * @return 受影响行数
     */
    public int saveOrUpdate(TaygedoAccount account) throws SQLException {
        String sql = """
            INSERT INTO taygedo_account (
                phone, name, device_id, openudid, vendorid,
                laohu_token, laohu_user_id, access_token, refresh_token, uid,
                role_id, role_name, server_id, server_name, game_id, gender,
                token_updated_at, created_at, updated_at
            ) VALUES (
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?,
                ?, ?, ?
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
                server_id = excluded.server_id,
                server_name = excluded.server_name,
                game_id = excluded.game_id,
                gender = excluded.gender,
                token_updated_at = excluded.token_updated_at,
                updated_at = excluded.updated_at
            """;

        long now = System.currentTimeMillis();
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.update(conn, sql,
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
                    account.getServerId(),
                    account.getServerName(),
                    account.getGameId(),
                    account.getGender(),
                    account.getTokenUpdatedAt(),
                    now,
                    now
            );
        }
    }

    /**
     * 根据手机号查询账号
     */
    public Optional<TaygedoAccount> findByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM taygedo_account WHERE phone = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            return Optional.ofNullable(
                    qr.query(conn, sql, new BeanHandler<>(TaygedoAccount.class, getRowProcessor()), phone));
        }
    }

    /**
     * 获取第一个账号
     */
    public Optional<TaygedoAccount> findFirst() throws SQLException {
        String sql = "SELECT * FROM taygedo_account LIMIT 1";
        try (Connection conn = JdbcUtils.getConnection()) {
            return Optional.ofNullable(
                    qr.query(conn, sql, new BeanHandler<>(TaygedoAccount.class, getRowProcessor())));
        }
    }

    /**
     * 获取所有账号
     */
    public List<TaygedoAccount> findAll() throws SQLException {
        String sql = "SELECT * FROM taygedo_account ORDER BY updated_at DESC";
        try (Connection conn = JdbcUtils.getConnection()) {
            List<TaygedoAccount> result = qr.query(conn, sql,
                    new BeanListHandler<>(TaygedoAccount.class, getRowProcessor()));
            return result != null ? result : Collections.emptyList();
        }
    }

    /**
     * 删除账号
     * @return true 表示删除了至少一行
     */
    public boolean delete(String phone) throws SQLException {
        String sql = "DELETE FROM taygedo_account WHERE phone = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            return qr.update(conn, sql, phone) > 0;
        }
    }
}
