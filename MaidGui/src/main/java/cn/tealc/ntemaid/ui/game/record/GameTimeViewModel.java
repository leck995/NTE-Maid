package cn.tealc.ntemaid.ui.game.record;

import cn.tealc.ntemaid.model.game.GameTime;
import cn.tealc.ntemaid.service.GameTimeService;
import cn.tealc.ntemaid.util.LanguageManager;
import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.time.Duration;
import java.util.*;


public class GameTimeViewModel implements ViewModel {
    private final ObservableList<XYChart.Series<String,Double>> chartData= FXCollections.observableArrayList();
    private final SimpleStringProperty allTotalTimeText=new SimpleStringProperty();
    private final SimpleStringProperty currentTotalTimeText=new SimpleStringProperty();
    private final SimpleStringProperty currentDayText=new SimpleStringProperty();
    private final SimpleStringProperty currentTimeText=new SimpleStringProperty();
    private final SimpleStringProperty currentUserName=new SimpleStringProperty();
    private final SimpleDoubleProperty currentProgressValue=new SimpleDoubleProperty();
    private final SimpleDoubleProperty totalProgressValue=new SimpleDoubleProperty();
    private final GameTimeService gameTimeService;

    @Inject
    public GameTimeViewModel(GameTimeService gameTimeService) {
        this.gameTimeService = gameTimeService;
        Thread.startVirtualThread(this::freshTotalData);
    }


    private void freshTotalData() {
        List<GameTime> allRecords = gameTimeService.getAllRecords();
        if (allRecords.isEmpty()) {
            Platform.runLater(() -> chartData.clear());
            return;
        }

        long totalMs = allRecords.stream().mapToLong(GameTime::getDuration).sum();
        double totalHours = totalMs / 3600000.0;

        Map<String, List<GameTime>> groupedMap = gameTimeService.getGroupedRecords();
        int dayCount = groupedMap.size();

        Map<String, List<GameTime>> last7Days = gameTimeService.getLastSevenDaysGroupedRecords();
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        series.setName(LanguageManager.getString("ui.game_time.total.charts.main_account"));
        last7Days.forEach((date, list) -> {
            long dayDuration = list.stream().mapToLong(GameTime::getDuration).sum();
            series.getData().add(new XYChart.Data<>(date, dayDuration / 60000.0));
        });

        long todayDuration = gameTimeService.getTodayTotalDuration();
        Duration duration = Duration.ofMillis(todayDuration);

        Platform.runLater(() -> {
            chartData.clear();
            chartData.add(series);
            allTotalTimeText.set(String.format("%.2f", totalHours));
            currentDayText.set(String.format(LanguageManager.getString("ui.game_time.account.days"), dayCount));
            currentTotalTimeText.set(String.format("%.2f", totalHours));
            totalProgressValue.set(1.0);
            currentTimeText.set(String.format(
                    LanguageManager.getString("ui.game_time.account.duration"),
                    duration.toHours(),
                    duration.toMinutesPart()
            ));
            currentProgressValue.set(duration.toMinutes() / 1440.0);
        });
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