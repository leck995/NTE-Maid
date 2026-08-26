package cn.tealc.ntemaid.service.system.impl;

import cn.tealc.ntemaid.dao.GameTimeDao;
import cn.tealc.ntemaid.model.game.GameTime;
import cn.tealc.ntemaid.service.system.GameTimeService;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameTimeServiceImpl implements GameTimeService {
    private static final Logger LOG = LoggerFactory.getLogger(GameTimeServiceImpl.class);
    private final GameTimeDao gameTimeDao;

    @Inject
    public GameTimeServiceImpl(GameTimeDao gameTimeDao) {
        this.gameTimeDao = gameTimeDao;
    }

    @Override
    public boolean addRecord(GameTime gameTime) {
        return gameTimeDao.addTime(gameTime).isPresent();
    }

    @Override
    public List<GameTime> getRecordsByDate(String date) {
        return gameTimeDao.getTimeListByDate(date);
    }

    @Override
    public List<GameTime> getAllRecords() {
        return gameTimeDao.getAllTime();
    }

    @Override
    public long getTotalDurationByDate(String date) {
        return gameTimeDao.getTimeListByDate(date).stream()
                .mapToLong(GameTime::getDuration)
                .sum();
    }

    @Override
    public Map<String, List<GameTime>> getGroupedRecords() {
        // 使用 LinkedHashMap 保持日期排序
        return gameTimeDao.getAllTime().stream()
                .collect(Collectors.groupingBy(
                        GameTime::getGameDate,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    @Override
    public Map<String, List<GameTime>> getLastSevenDaysGroupedRecords() {
        // 简单的逻辑：取最后 7 个不同的日期
        Map<String, List<GameTime>> grouped = getGroupedRecords();
        List<String> keys = new ArrayList<>(grouped.keySet());
        int start = Math.max(0, keys.size() - 7);

        Map<String, List<GameTime>> lastSeven = new LinkedHashMap<>();
        for (int i = start; i < keys.size(); i++) {
            String key = keys.get(i);
            lastSeven.put(key, grouped.get(key));
        }
        return lastSeven;
    }

    @Override
    public long getTodayTotalDuration() {
        String today = LocalDate.now().toString();
        return gameTimeDao.getTimeListByDate(today).stream()
                .mapToLong(GameTime::getDuration)
                .sum();
    }

    @Override
    public void saveSession(LocalDateTime start, LocalDateTime end) {
        long totalDuration = java.time.Duration.between(start, end).toMillis();
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        if (startDate.isBefore(endDate)) {
            // 处理跨天逻辑
            LocalDateTime endOfFirstDay = startDate.plusDays(1).atStartOfDay();
            long firstDayMillis = java.time.Duration.between(start, endOfFirstDay).toMillis();

            // 保存第一天
            addRecord(new GameTime(startDate.toString(),
                    start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    endOfFirstDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    firstDayMillis));

            // 保存第二天
            addRecord(new GameTime(endDate.toString(),
                    endOfFirstDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    totalDuration - firstDayMillis));
        } else {
            // 普通情况
            addRecord(new GameTime(startDate.toString(),
                    start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    totalDuration));
        }
    }
}