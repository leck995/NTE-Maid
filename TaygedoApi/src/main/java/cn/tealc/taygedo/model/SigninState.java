package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 签到状态
 * 当前月份已签到的累计天数
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SigninState {
    /** 本月已签到天数（用于定位当天奖励在奖励列表中的位置） */
    private int days;
    private int day;
    private int month;
    private boolean todaySign;

    public SigninState() {
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public boolean isTodaySign() {
        return todaySign;
    }

    public void setTodaySign(boolean todaySign) {
        this.todaySign = todaySign;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }
}
