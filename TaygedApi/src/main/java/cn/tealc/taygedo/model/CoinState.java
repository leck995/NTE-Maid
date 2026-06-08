package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 金币状态
 * 当日金币获取汇总信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinState {
    /** 今日已获得金币数 */
    @JsonProperty("todayCoin")
    private int todayCoin;
    /** 每日金币上限 */
    @JsonProperty("limitCoin")
    private int limitCoin;

    public CoinState() {
    }

    public int getTodayCoin() {
        return todayCoin;
    }

    public void setTodayCoin(int todayCoin) {
        this.todayCoin = todayCoin;
    }

    public int getLimitCoin() {
        return limitCoin;
    }

    public void setLimitCoin(int limitCoin) {
        this.limitCoin = limitCoin;
    }
}
