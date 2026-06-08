package cn.tealc.ntemaid.dao;

import cn.tealc.ntemaid.model.game.GameTime;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class GameTimeDao {
    private static final Logger LOG = LoggerFactory.getLogger(GameTimeDao.class);
    private final QueryRunner qr = new QueryRunner();

    private RowProcessor getRowProcessor() {
        Map<String, String> map = new HashMap<>();
        map.put("game_date", "gameDate");
        map.put("start_time", "startTime");
        map.put("end_time", "endTime");
        map.put("duration", "duration");
        return new BasicRowProcessor(new BeanProcessor(map));
    }

    public List<GameTime> getTimeListByDate(String date) {
        String sql = "SELECT * FROM game_time WHERE game_date = ?";
        try (Connection conn = JdbcUtils.getConnection()) {
            List<GameTime> result = qr.query(conn, sql, new BeanListHandler<>(GameTime.class, getRowProcessor()), date);
            return result != null ? result : Collections.emptyList(); // 关键改动：绝不返回 null
        } catch (SQLException e) {
            LOG.error("查询日期 {} 失败", date, e);
            return Collections.emptyList();
        }
    }

    public Optional<Integer> addTime(GameTime gameTime) {
        String sql = "INSERT OR IGNORE INTO game_time (game_date, start_time, end_time, duration) VALUES (?,?,?,?)";
        try (Connection conn = JdbcUtils.getConnection()) {
            Number id = qr.insert(conn, sql, new ScalarHandler<Number>(),
                    gameTime.getGameDate(),
                    gameTime.getStartTime(),
                    gameTime.getEndTime(),
                    gameTime.getDuration());
            return Optional.ofNullable(id).map(Number::intValue);
        } catch (SQLException e) {
            LOG.error("添加游戏记录失败", e);
            return Optional.empty();
        }
    }

    public List<GameTime> getAllTime() {
        String sql = "SELECT * FROM game_time";
        try (Connection conn = JdbcUtils.getConnection()) {
            List<GameTime> result = qr.query(conn, sql, new BeanListHandler<>(GameTime.class, getRowProcessor()));
            return result != null ? result : Collections.emptyList();
        } catch (SQLException e) {
            LOG.error("获取记录失败", e);
            return Collections.emptyList();
        }
    }
}