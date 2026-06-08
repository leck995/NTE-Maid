package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 金币任务
 * 每日可完成的金币获取任务（签到、浏览、点赞、分享等）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinTask {
    /** 任务编码（如 signin_c, browse_post_c, like_post_c, share） */
    private String code;
    /** 已完成次数 */
    private int completeTimes;
    /** 每日上限次数 */
    private int limitTimes;

    public CoinTask() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getCompleteTimes() {
        return completeTimes;
    }

    public void setCompleteTimes(int completeTimes) {
        this.completeTimes = completeTimes;
    }

    public int getLimitTimes() {
        return limitTimes;
    }

    public void setLimitTimes(int limitTimes) {
        this.limitTimes = limitTimes;
    }
}
