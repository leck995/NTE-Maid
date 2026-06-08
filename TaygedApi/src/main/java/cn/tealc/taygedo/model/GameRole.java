package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 游戏角色
 * 用户在某个游戏中的绑定角色信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameRole {
    /** 角色ID，用于游戏签到 */
    private String roleId;
    /** 角色名称 */
    private String roleName;

    public GameRole() {
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
