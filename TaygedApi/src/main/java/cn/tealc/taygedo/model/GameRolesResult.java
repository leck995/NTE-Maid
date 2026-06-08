package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 游戏角色列表
 * 查询某个游戏下所有已绑定角色的结果
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameRolesResult {
    /** 角色列表（已过滤roleId为null的无效条目） */
    private List<GameRole> roles;

    public GameRolesResult() {
    }

    public List<GameRole> getRoles() {
        return roles;
    }

    public void setRoles(List<GameRole> roles) {
        this.roles = roles;
    }
}
