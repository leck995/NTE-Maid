package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 老虎平台登录结果
 * 通过短信验证码或密码登录老虎用户中心后返回的凭证
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResult {
    /** 老虎平台令牌，用于换取塔吉多accessToken */
    private String token;
    /** 老虎平台用户ID */
    private String userId;
    /** 用户昵称 */
    private String nickname;
    /** 手机号（脱敏后） */
    private String cellphone;
    /** 头像URL */
    private String headImg;

    public LoginResult() {
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getCellphone() { return cellphone; }
    public void setCellphone(String cellphone) { this.cellphone = cellphone; }

    public String getHeadImg() { return headImg; }
    public void setHeadImg(String headImg) { this.headImg = headImg; }
}
