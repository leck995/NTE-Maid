package cn.tealc.ntemaid.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class JdbcUtils {
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
        dataSource = new HikariDataSource(config);
        initDatabase();
    }

    private static void initDatabase() {
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {
            // 1. 设置性能模式
            st.execute("PRAGMA journal_mode=WAL;");
            st.execute("PRAGMA synchronous=NORMAL;");

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
                """;
            for (String s : sql.split(";")) {
                if (!s.trim().isEmpty()) st.execute(s);
            }
        } catch (SQLException e) {
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    /**
     * 从池中获取连接
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 程序退出时彻底释放池
     */
    public static void exit() {
        if (dataSource != null) {
            dataSource.close();
        }
    }


}