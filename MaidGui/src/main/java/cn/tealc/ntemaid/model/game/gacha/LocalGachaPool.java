package cn.tealc.ntemaid.model.game.gacha;

import java.util.List;

public class LocalGachaPool {
    private String poolName;
    private LocalGachaType type;
    private int count;
    private int max;
    private int ssrCount;
    private double ssrPercent;
    private double ssrAvg;
    private double upSsrAvg;//限定武器up率
    private double upSsrCount;//限定武器数量率
    private double noUpSsrCount;//常驻武器数量
    private int luckyType;//抽卡运气
    private String time;//抽卡起始结束时间

    private List<LocalGachaItem> items;

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

    public List<LocalGachaItem> getItems() {
        return items;
    }

    public void setItems(List<LocalGachaItem> items) {
        this.items = items;
    }

    public LocalGachaType getType() {
        return type;
    }

    public void setType(LocalGachaType type) {
        this.type = type;
    }

    public double getUpSsrAvg() {
        return upSsrAvg;
    }

    public void setUpSsrAvg(double upSsrAvg) {
        this.upSsrAvg = upSsrAvg;
    }

    public double getUpSsrCount() {
        return upSsrCount;
    }

    public void setUpSsrCount(double upSsrCount) {
        this.upSsrCount = upSsrCount;
    }

    public double getNoUpSsrCount() {
        return noUpSsrCount;
    }

    public void setNoUpSsrCount(double noUpSsrCount) {
        this.noUpSsrCount = noUpSsrCount;
    }

    public int getLuckyType() {
        return luckyType;
    }

    public void setLuckyType(int luckyType) {
        this.luckyType = luckyType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
