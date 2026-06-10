package cn.tealc.ntemaid.model.taygedo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 塔吉多账号信息
 * 包含设备标识、登录凭证，支持持久化到本地JSON文件
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaygedoAccount {
    /** 手机号 */
    private String phone;
    /** 账号名称（用于多账号管理） */
    private String name;
    /** 设备唯一标识（32位十六进制） */
    private String deviceId;
    /** 塔吉多访问令牌 */
    private String accessToken;
    /** 塔吉多刷新令牌 */
    private String refreshToken;
    /** 塔吉多用户UID */
    private String uid;
    /** 上次绑定的角色ID */
    private String roleId;
    /** 上次绑定的角色名称 */
    private String roleName;
    /** 服务器ID */
    private String serverId;
    /** 服务器名称 */
    private String serverName;
    /** 游戏ID */
    private String gameId;
    /** 性别 */
    private String gender;
    /** Token更新时间（上海时区） */
    private String tokenUpdatedAt;
    /** 最后签到时间（毫秒时间戳） */
    private Long lastSignTime;

    public TaygedoAccount() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTokenUpdatedAt() {
        return tokenUpdatedAt;
    }

    public void setTokenUpdatedAt(String tokenUpdatedAt) {
        this.tokenUpdatedAt = tokenUpdatedAt;
    }

    public Long getLastSignTime() {
        return lastSignTime;
    }

    public void setLastSignTime(Long lastSignTime) {
        this.lastSignTime = lastSignTime;
    }

    /** 是否已有有效的塔吉多登录凭证 */
    public boolean hasTajiduoTokens() {
        return accessToken != null && !accessToken.isBlank()
                && refreshToken != null && !refreshToken.isBlank();
    }
}
