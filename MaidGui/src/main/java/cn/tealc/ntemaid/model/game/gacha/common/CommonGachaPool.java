package cn.tealc.ntemaid.model.game.gacha.common;

import cn.tealc.ntemaid.model.game.gacha.local.LocalGachaItem;
import cn.tealc.ntemaid.model.game.gacha.local.LocalGachaType;

import java.util.List;

public class CommonGachaPool {
    private String poolName;
    private LocalGachaType type;
    private int max; //卡池保底最大抽数
    private int luckyType;//抽卡运气
    private String time;//抽卡起始结束时间

    private Integer totalCount = 0; //总抽数
    private Integer noUpSsrSize= 0; //当前未出货抽数
    private Integer noUpSrSize = 0; //当前未出货抽数
    private Integer noUpRSize= 0; //当前未出货抽数

    private List<CommonGachaItem> ssrDataList; //五星
    private List<CommonGachaItem> srDataList; //四星
    private List<CommonGachaItem> rDataList; //三星

    private Integer ssrCount = 0; //五星数量
    private Integer srCount = 0;
    private Integer rCount = 0;

    private Double ssrAvg = 0.0; //五星平均抽数
    private Integer ssrMin = 0;//五星最小抽数
    private Integer ssrMax = 0;//五星最大抽数
    private Integer upSsrCount = 0; //UP五星数量
    private Double upSsrAvg = 0.0; //UP五星平均抽数
    private double noUpSsrCount;//常驻武器数量
    private Double nonBannerRate = 0.0; //五星不歪率

    private Double srAvg = 0.0; //四星平均抽数
    private Integer srMin = 0;//四星最小抽数
    private Integer srMax = 0;//四星最大抽数
    private Double rAvg = 0.0; //三星平均抽数
    private Integer rMin = 0;//三星最小抽数
    private Integer rMax = 0;//三星最大抽数


    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public LocalGachaType getType() {
        return type;
    }

    public void setType(LocalGachaType type) {
        this.type = type;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getLuckyType() {
        return luckyType;
    }

    public void setLuckyType(int luckyType) {
        this.luckyType = luckyType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getNoUpSsrSize() {
        return noUpSsrSize;
    }

    public void setNoUpSsrSize(Integer noUpSsrSize) {
        this.noUpSsrSize = noUpSsrSize;
    }

    public Integer getNoUpSrSize() {
        return noUpSrSize;
    }

    public void setNoUpSrSize(Integer noUpSrSize) {
        this.noUpSrSize = noUpSrSize;
    }

    public Integer getNoUpRSize() {
        return noUpRSize;
    }

    public void setNoUpRSize(Integer noUpRSize) {
        this.noUpRSize = noUpRSize;
    }

    public List<CommonGachaItem> getSsrDataList() {
        return ssrDataList;
    }

    public void setSsrDataList(List<CommonGachaItem> ssrDataList) {
        this.ssrDataList = ssrDataList;
    }

    public List<CommonGachaItem> getSrDataList() {
        return srDataList;
    }

    public void setSrDataList(List<CommonGachaItem> srDataList) {
        this.srDataList = srDataList;
    }

    public List<CommonGachaItem> getrDataList() {
        return rDataList;
    }

    public void setrDataList(List<CommonGachaItem> rDataList) {
        this.rDataList = rDataList;
    }

    public Integer getSsrCount() {
        return ssrCount;
    }

    public void setSsrCount(Integer ssrCount) {
        this.ssrCount = ssrCount;
    }

    public Integer getSrCount() {
        return srCount;
    }

    public void setSrCount(Integer srCount) {
        this.srCount = srCount;
    }

    public Integer getrCount() {
        return rCount;
    }

    public void setrCount(Integer rCount) {
        this.rCount = rCount;
    }

    public Double getSsrAvg() {
        return ssrAvg;
    }

    public void setSsrAvg(Double ssrAvg) {
        this.ssrAvg = ssrAvg;
    }

    public Integer getSsrMin() {
        return ssrMin;
    }

    public void setSsrMin(Integer ssrMin) {
        this.ssrMin = ssrMin;
    }

    public Integer getSsrMax() {
        return ssrMax;
    }

    public void setSsrMax(Integer ssrMax) {
        this.ssrMax = ssrMax;
    }

    public Integer getUpSsrCount() {
        return upSsrCount;
    }

    public void setUpSsrCount(Integer upSsrCount) {
        this.upSsrCount = upSsrCount;
    }

    public Double getUpSsrAvg() {
        return upSsrAvg;
    }

    public void setUpSsrAvg(Double upSsrAvg) {
        this.upSsrAvg = upSsrAvg;
    }

    public double getNoUpSsrCount() {
        return noUpSsrCount;
    }

    public void setNoUpSsrCount(double noUpSsrCount) {
        this.noUpSsrCount = noUpSsrCount;
    }

    public Double getNonBannerRate() {
        return nonBannerRate;
    }

    public void setNonBannerRate(Double nonBannerRate) {
        this.nonBannerRate = nonBannerRate;
    }

    public Double getSrAvg() {
        return srAvg;
    }

    public void setSrAvg(Double srAvg) {
        this.srAvg = srAvg;
    }

    public Integer getSrMin() {
        return srMin;
    }

    public void setSrMin(Integer srMin) {
        this.srMin = srMin;
    }

    public Integer getSrMax() {
        return srMax;
    }

    public void setSrMax(Integer srMax) {
        this.srMax = srMax;
    }

    public Double getrAvg() {
        return rAvg;
    }

    public void setrAvg(Double rAvg) {
        this.rAvg = rAvg;
    }

    public Integer getrMin() {
        return rMin;
    }

    public void setrMin(Integer rMin) {
        this.rMin = rMin;
    }

    public Integer getrMax() {
        return rMax;
    }

    public void setrMax(Integer rMax) {
        this.rMax = rMax;
    }
}
