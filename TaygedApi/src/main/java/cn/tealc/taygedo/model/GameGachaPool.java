package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 单个卡池抽卡汇总
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameGachaPool {
    private String tab;
    private int drawCount;
    private int m;
    private String average;
    private int rareCount;
    private String playerOver;
    private List<GameGachaItem> details;

    public GameGachaPool() {}

    public String getTab() { return tab; }
    public void setTab(String tab) { this.tab = tab; }
    public int getDrawCount() { return drawCount; }
    public void setDrawCount(int drawCount) { this.drawCount = drawCount; }
    public int getM() { return m; }
    public void setM(int m) { this.m = m; }
    public String getAverage() { return average; }
    public void setAverage(String average) { this.average = average; }
    public int getRareCount() { return rareCount; }
    public void setRareCount(int rareCount) { this.rareCount = rareCount; }
    public String getPlayerOver() { return playerOver; }
    public void setPlayerOver(String playerOver) { this.playerOver = playerOver; }
    public List<GameGachaItem> getDetails() { return details; }
    public void setDetails(List<GameGachaItem> details) { this.details = details; }
}
