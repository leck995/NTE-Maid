package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 塔吉多用户中心登录结果
 * 使用老虎token换取塔吉多的访问令牌和刷新令牌
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCenterLoginResult {
    /** 塔吉多访问令牌，调用业务API时携带 */
    private String accessToken;
    /** 塔吉多刷新令牌，用于accessToken过期后刷新 */
    private String refreshToken;
    /** 塔吉多用户ID */
    private String uid;

    public UserCenterLoginResult() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
