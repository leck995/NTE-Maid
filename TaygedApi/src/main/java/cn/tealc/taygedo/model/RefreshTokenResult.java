package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Token刷新结果
 * 使用refreshToken刷新后返回新的访问令牌和刷新令牌
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefreshTokenResult {
    /** 新的塔吉多访问令牌 */
    private String accessToken;
    /** 新的塔吉多刷新令牌 */
    private String refreshToken;
    /** 塔吉多用户ID（可能为空） */
    private String uid;

    public RefreshTokenResult() {
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
