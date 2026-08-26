package cn.tealc.ntemaid.service.system;

import cn.tealc.ntemaid.model.game.GameTime;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface GameTimeService {
    /**
     * 保存游戏记录
     * @return 返回保存成功后的ID，如果已存在或失败则返回 null/empty 相关逻辑
     */
    boolean addRecord(GameTime gameTime);

    /**
     * 获取指定日期的记录，确保不返回 null
     */
    List<GameTime> getRecordsByDate(String date);

    /**
     * 获取所有记录
     */
    List<GameTime> getAllRecords();

    /**
     * 统计指定日期的总时长（分钟）
     */
    long getTotalDurationByDate(String date);

    Map<String, List<GameTime>> getGroupedRecords();

    /** 获取最近 7 天的统计数据 */
    Map<String, List<GameTime>> getLastSevenDaysGroupedRecords();
    /** 获取今日总时长（毫秒） */
    long getTodayTotalDuration();
    void saveSession(LocalDateTime start, LocalDateTime end);
}