package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 签到奖励
 * 某一天签到可获得的奖励内容
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SigninReward {
    /** 奖励名称 */
    private String name;
    /** 奖励数量 */
    private int num;

    public SigninReward() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
