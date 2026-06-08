package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * APP签到结果
 * 塔吉多App每日签到后获得的经验值和金币
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppSigninResult {
    /** 获得的经验值 */
    private int exp;
    /** 获得的金币数 */
    @JsonProperty("goldCoin")
    private int goldCoin;

    public AppSigninResult() {
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getGoldCoin() {
        return goldCoin;
    }

    public void setGoldCoin(int goldCoin) {
        this.goldCoin = goldCoin;
    }
}
