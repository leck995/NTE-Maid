package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 绑定角色信息
 * 用户在当前游戏中绑定的主角色
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BindRoleInfo {
    /** 角色ID */
    private String roleId;
    /** 角色名称 */
    private String roleName;

    public BindRoleInfo() {
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
