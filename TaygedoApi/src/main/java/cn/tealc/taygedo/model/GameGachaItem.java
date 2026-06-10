package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 单条抽卡记录
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameGachaItem {
    private String charid;
    private int luckyType;
    private int rareCount;
    private String time;
    private long timeStamp;

    public GameGachaItem() {}

    public String getCharid() { return charid; }
    public void setCharid(String charid) { this.charid = charid; }
    public int getLuckyType() { return luckyType; }
    public void setLuckyType(int luckyType) { this.luckyType = luckyType; }
    public int getRareCount() { return rareCount; }
    public void setRareCount(int rareCount) { this.rareCount = rareCount; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public long getTimeStamp() { return timeStamp; }
    public void setTimeStamp(long timeStamp) { this.timeStamp = timeStamp; }
}
