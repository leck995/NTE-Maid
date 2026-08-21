package cn.tealc.ntemaid.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcUtils.class);
    private static final String DB_URL = "jdbc:sqlite:data.db";
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(3);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        // 【核心修改 1】通过连接池配置，确保每个连接创建后立即开启外键支持
        // 这样你就不需要每次手动 execute("PRAGMA foreign_keys = ON")
        config.setConnectionInitSql("PRAGMA foreign_keys = ON;");

        dataSource = new HikariDataSource(config);
        initDatabase();
    }

    private static void initDatabase() {
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {

            // 【核心修改 2】设置性能模式及确保当前连接开启外键
            st.execute("PRAGMA journal_mode=WAL;");
            st.execute("PRAGMA synchronous=NORMAL;");
            st.execute("PRAGMA foreign_keys = ON;"); // 双重保证

            String sql = """
                CREATE TABLE IF NOT EXISTS game_time(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    game_date VARCHAR(100),
                    start_time BIGINT NOT NULL,
                    end_time BIGINT NOT NULL,
                    duration BIGINT NOT NULL);
                
                CREATE TABLE IF NOT EXISTS user_info(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id VARCHAR(20),
                    role_id VARCHAR(20) UNIQUE,
                    token VARCHAR(255),
                    is_main BOOL DEFAULT false,
                    last_sign_time INTEGER,
                    is_web BOOL DEFAULT false);
                
                CREATE TABLE IF NOT EXISTS config (
                    key VARCHAR NOT NULL UNIQUE,
                    value VARCHAR
                );
                
                CREATE TABLE IF NOT EXISTS music (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title VARCHAR(255) NOT NULL,
                    artist VARCHAR(255),
                    album VARCHAR(255),
                    duration INTEGER,
                    file_path TEXT UNIQUE,
                    add_time BIGINT
                );

                CREATE TABLE IF NOT EXISTS playlist (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(100) NOT NULL UNIQUE,
                    description VARCHAR(255),
                    type VARCHAR(255),
                    cover_path VARCHAR(255),
                    create_time BIGINT
                );

                CREATE TABLE IF NOT EXISTS playlist_music_relation (
                    playlist_id INTEGER,
                    music_id INTEGER,
                    sort_order INTEGER,
                    PRIMARY KEY (playlist_id, music_id),
                    FOREIGN KEY (playlist_id) REFERENCES playlist(id) ON DELETE CASCADE,
                    FOREIGN KEY (music_id) REFERENCES music(id) ON DELETE CASCADE
                );

                CREATE TABLE IF NOT EXISTS playing_list (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    music_id INTEGER NOT NULL,
                    sort_order INTEGER NOT NULL,
                    UNIQUE(music_id),
                    FOREIGN KEY (music_id) REFERENCES music(id) ON DELETE CASCADE
                );

                CREATE TABLE IF NOT EXISTS game_gacha (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    role_id VARCHAR(64) NOT NULL,
                    gacha_type INTEGER NOT NULL DEFAULT 1,
                    charid VARCHAR(64),
                    lucky_type INTEGER DEFAULT 0,
                    rare_count INTEGER DEFAULT 0,
                    time VARCHAR(32),
                    time_stamp BIGINT
                );

                CREATE TABLE IF NOT EXISTS game_common_gacha (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_id VARCHAR(64) NOT NULL,
                    record_id TEXT,
                    record_type VARCHAR(32),
                    time VARCHAR(32) NOT NULL,
                    pool_id VARCHAR(64),
                    pool_name VARCHAR(64),
                    item_id VARCHAR(64),
                    item_name VARCHAR(64),
                    count INTEGER DEFAULT 1,
                    roll_points INTEGER DEFAULT 0,
                    roll_label VARCHAR(32),
                    sort INTEGER DEFAULT 0
                );

                CREATE UNIQUE INDEX IF NOT EXISTS idx_common_gacha_unique
                ON game_common_gacha(player_id, time, sort);

                CREATE TABLE IF NOT EXISTS taygedo_account (
                    phone VARCHAR(20) PRIMARY KEY,
                    name VARCHAR(100),
                    device_id VARCHAR(64),
                    access_token TEXT,
                    refresh_token TEXT,
                    uid VARCHAR(64),
                    role_id VARCHAR(64),
                    role_name VARCHAR(100),
                    server_id VARCHAR(64),
                    server_name VARCHAR(100),
                    game_id VARCHAR(32),
                    gender VARCHAR(16),
                    last_sign_time BIGINT,
                    token_updated_at VARCHAR(32),
                    created_at BIGINT,
                    updated_at BIGINT
                );
                """;

            for (String s : sql.split(";")) {
                if (!s.trim().isEmpty()) st.execute(s);
            }

            // 迁移：历史 data.db 中曾将 idx_common_gacha_unique 错误地建在 common_gacha 表上，
            // 由于 CREATE ... IF NOT EXISTS 按索引名判定，导致 game_common_gacha 上的同名索引被静默跳过、
            // INSERT OR IGNORE 去重失效、第二次抓取的记录被叠加入库。
            // 这里在普通建表流程之外补建正确的索引，并清理已产生的重复行。
            migrateCommonGachaUniqueIndex(st);

            LOG.info("数据库初始化完成，外键级联删除已启用");
        } catch (SQLException e) {
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    /**
     * 修复 game_common_gacha 的唯一索引缺失问题。
     * <p>历史数据库中 {@code idx_common_gacha_unique} 曾被建在已废弃的 {@code common_gacha}
     * 表上，使得 {@code CREATE UNIQUE INDEX IF NOT EXISTS ... ON game_common_gacha} 因索引名
     * 已存在而静默跳过，{@code INSERT OR IGNORE} 失去去重作用，导致重复抓取入库。
     * <p>本方法：<ol>
     *   <li>删除挂在错误表（common_gacha）上的同名索引；</li>
     *   <li>清理 game_common_gacha 中 (player_id, time, sort) 重复的行，仅保留最小 id；</li>
     *   <li>在 game_common_gacha 上重建唯一索引。</li>
     * </ol>
     * 重复行存在时直接 CREATE UNIQUE INDEX 会失败，故必须先清理。整个操作幂等，可重复执行。
     */
    private static void migrateCommonGachaUniqueIndex(Statement st) throws SQLException {
        // 删除可能错挂在 common_gacha（或任何表）上的同名旧索引，确保后续按表名重建不被名字挡住
        st.execute("DROP INDEX IF EXISTS idx_common_gacha_unique");

        // 清理重复行：对同一 (player_id, time, sort) 保留最小 id，删除其余
        st.execute("""
            DELETE FROM game_common_gacha
            WHERE id NOT IN (
                SELECT MIN(id) FROM game_common_gacha
                GROUP BY player_id, time, sort
            )
            """);

        // 在正确的表上重建唯一索引
        st.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_common_gacha_unique " +
                   "ON game_common_gacha(player_id, time, sort)");
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void exit() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}