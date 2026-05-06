package cn.tealc.ntemaid.model.game;

/**
 * @description:
 * @author: Leck
 * @create: 2024-07-11 00:01
 */
public class GameTime {
    private Integer id;
    private String gameDate;//当天日期
    private Long startTime; //开始时间
    private Long endTime;//结束时间
    private Long duration;//持续时长

    public GameTime() {
    }

    public GameTime(String gameDate, Long startTime, Long endTime, Long duration) {
        this.gameDate = gameDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGameDate() {
        return gameDate;
    }

    public void setGameDate(String gameDate) {
        this.gameDate = gameDate;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "GameTime{" +
                ", gameData='" + gameDate + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + duration +
                '}';
    }
}