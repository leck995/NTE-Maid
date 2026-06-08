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

                CREATE TABLE IF NOT EXISTS taygedo_account (
                    phone VARCHAR(20) PRIMARY KEY,
                    name VARCHAR(100),
                    device_id VARCHAR(64),
                    openudid VARCHAR(64),
                    vendorid VARCHAR(64),
                    laohu_token TEXT,
                    laohu_user_id VARCHAR(64),
                    access_token TEXT,
                    refresh_token TEXT,
                    uid VARCHAR(64),
                    role_id VARCHAR(64),
                    role_name VARCHAR(100),
                    server_id VARCHAR(64),
                    server_name VARCHAR(100),
                    game_id VARCHAR(32),
                    gender VARCHAR(16),
                    token_updated_at VARCHAR(32),
                    created_at BIGINT,
                    updated_at BIGINT
                );
                """;

            for (String s : sql.split(";")) {
                if (!s.trim().isEmpty()) st.execute(s);
            }

            // 迁移：为旧数据库添加新列
            String[] migrations = {
                "ALTER TABLE taygedo_account ADD COLUMN server_id VARCHAR(64)",
                "ALTER TABLE taygedo_account ADD COLUMN server_name VARCHAR(100)",
                "ALTER TABLE taygedo_account ADD COLUMN game_id VARCHAR(32)",
                "ALTER TABLE taygedo_account ADD COLUMN gender VARCHAR(16)"
            };
            for (String migration : migrations) {
                try {
                    st.execute(migration);
                } catch (SQLException ignored) {
                    // 列已存在时忽略
                }
            }

            LOG.info("数据库初始化完成，外键级联删除已启用");
        } catch (SQLException e) {
            throw new RuntimeException("数据库初始化失败", e);
        }
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