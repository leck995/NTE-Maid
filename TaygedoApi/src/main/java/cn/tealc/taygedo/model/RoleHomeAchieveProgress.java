package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 角色综合面板中的成就进度总览
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleHomeAchieveProgress {
    /** 已达成成就数 */
    @JsonProperty("achievementCnt")
    private int achievementCnt;
    /** 成就总数 */
    private int total;

    public RoleHomeAchieveProgress() {}

    public int getAchievementCnt() { return achievementCnt; }
    public void setAchievementCnt(int achievementCnt) { this.achievementCnt = achievementCnt; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
