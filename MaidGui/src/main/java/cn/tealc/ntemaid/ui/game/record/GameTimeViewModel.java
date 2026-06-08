package cn.tealc.ntemaid.ui.game.record;

import cn.tealc.ntemaid.dao.GameTimeDao;
import cn.tealc.ntemaid.model.game.GameTime;
import cn.tealc.ntemaid.service.GameTimeService;
import cn.tealc.ntemaid.service.impl.GameTimeServiceImpl;
import cn.tealc.ntemaid.util.LanguageManager;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @program: WutheringWavesTool
 * @description:
 * @author: Leck
 * @create: 2024-08-04 04:57
 */
public class GameTimeViewModel implements ViewModel {
    private final ObservableList<XYChart.Series<String,Double>> chartData= FXCollections.observableArrayList();
    private final SimpleStringProperty allTotalTimeText=new SimpleStringProperty();
    private final SimpleStringProperty currentTotalTimeText=new SimpleStringProperty();
    private final SimpleStringProperty currentDayText=new SimpleStringProperty();
    private final SimpleStringProperty currentTimeText=new SimpleStringProperty();
    private final SimpleStringProperty currentUserName=new SimpleStringProperty();
    private final SimpleDoubleProperty currentProgressValue=new SimpleDoubleProperty();
    private final SimpleDoubleProperty totalProgressValue=new SimpleDoubleProperty();
    private final GameTimeService gameTimeService = new GameTimeServiceImpl();
    public GameTimeViewModel() {
        freshTotalData();
    }



    /**
     * @description: 更新用户数据
     * @param:
     * @return  void
     * @date:   2024/10/8
     */
    private void freshTotalData() {
        chartData.clear();
        List<GameTime> allRecords = gameTimeService.getAllRecords();
        if (allRecords.isEmpty()) return;

        // 1. 统计总时长（小时）
        long totalMs = allRecords.stream().mapToLong(GameTime::getDuration).sum();
        allTotalTimeText.set(String.format("%.2f", totalMs / 3600000.0));

        // 2. 统计总天数
        Map<String, List<GameTime>> groupedMap = gameTimeService.getGroupedRecords();
        currentDayText.set(String.format(LanguageManager.getString("ui.game_time.account.days"), groupedMap.size()));

        // 3. 更新图表 (近七日)
        updateChartData(gameTimeService.getLastSevenDaysGroupedRecords(),
                LanguageManager.getString("ui.game_time.total.charts.main_account"));

        // 4. 当前账号统计（这里假设 mainMap 就是 groupedMap，根据你具体逻辑调整）
        double currentTotalTimeHours = totalMs / 3600000.0;
        currentTotalTimeText.set(String.format("%.2f", currentTotalTimeHours));
        totalProgressValue.set(1.0); // 比例逻辑

        // 5. 更新今日详情
        updateCurrentGameTime();
    }

    private void updateChartData(Map<String, List<GameTime>> map, String name) {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        series.setName(name);

        map.forEach((date, list) -> {
            long dayDuration = list.stream().mapToLong(GameTime::getDuration).sum();
            series.getData().add(new XYChart.Data<>(date, dayDuration / 60000.0)); // 转为分钟显示
        });
        chartData.add(series);
    }

    /**
     * @description: 获取当前账号今日游玩时间
     * @param:
     * @return  void
     * @date:   2024/8/4
     */
    private void updateCurrentGameTime() {
        long sum = gameTimeService.getTodayTotalDuration();
        Duration duration = Duration.ofMillis(sum);
        long hour = duration.toHours();
        long minute = duration.toMinutesPart();
        currentProgressValue.set(duration.toMinutes() / 1440.0);
        currentTimeText.set(String.format(
                LanguageManager.getString("ui.game_time.account.duration"),
                hour,
                minute
        ));
    }


    public ObservableList<XYChart.Series<String, Double>> getChartData() {
        return chartData;
    }

    public String getAllTotalTimeText() {
        return allTotalTimeText.get();
    }

    public SimpleStringProperty allTotalTimeTextProperty() {
        return allTotalTimeText;
    }

    public String getCurrentTotalTimeText() {
        return currentTotalTimeText.get();
    }

    public SimpleStringProperty currentTotalTimeTextProperty() {
        return currentTotalTimeText;
    }

    public String getCurrentDayText() {
        return currentDayText.get();
    }

    public SimpleStringProperty currentDayTextProperty() {
        return currentDayText;
    }

    public String getCurrentTimeText() {
        return currentTimeText.get();
    }

    public SimpleStringProperty currentTimeTextProperty() {
        return currentTimeText;
    }

    public double getCurrentProgressValue() {
        return currentProgressValue.get();
    }

    public SimpleDoubleProperty currentProgressValueProperty() {
        return currentProgressValue;
    }

    public double getTotalProgressValue() {
        return totalProgressValue.get();
    }

    public SimpleDoubleProperty totalProgressValueProperty() {
        return totalProgressValue;
    }

    public String getCurrentUserName() {
        return currentUserName.get();
    }

    public SimpleStringProperty currentUserNameProperty() {
        return currentUserName;
    }
}