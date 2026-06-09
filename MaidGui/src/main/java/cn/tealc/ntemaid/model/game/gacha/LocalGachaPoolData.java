package cn.tealc.ntemaid.model.game.gacha;

import java.util.List;

public class LocalGachaPoolData {
    private String poolName;
    private int max;
    private int ssrCount;
    private double ssrPercent;
    private double ssrAvg;
    private List<LocalGachaData> items;

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getSsrCount() {
        return ssrCount;
    }

    public void setSsrCount(int ssrCount) {
        this.ssrCount = ssrCount;
    }

    public double getSsrPercent() {
        return ssrPercent;
    }

    public void setSsrPercent(double ssrPercent) {
        this.ssrPercent = ssrPercent;
    }

    public double getSsrAvg() {
        return ssrAvg;
    }

    public void setSsrAvg(double ssrAvg) {
        this.ssrAvg = ssrAvg;
    }

    public List<LocalGachaData> getItems() {
        return items;
    }

    public void setItems(List<LocalGachaData> items) {
        this.items = items;
    }
}
