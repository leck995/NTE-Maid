package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 游戏角色卡
 * 从游戏记录卡接口获取的角色绑定信息，作为getGameRoles的补充数据源
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameRecordCard {
    /** 游戏ID */
    private String gameId;
    /** 游戏名称 */
    private String gameName;
    /** 角色ID（从bindRoleInfo中提取） */
    private String roleId;
    /** 角色名称（从bindRoleInfo中提取） */
    private String roleName;

    public GameRecordCard() {
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
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
}
