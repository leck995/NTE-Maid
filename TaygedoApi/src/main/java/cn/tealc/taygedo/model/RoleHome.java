package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 异环角色综合面板：头像/等级/成就总览/区域总览/房产/载具/角色简版
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleHome {
    /** 用户 ID */
    private String userid;
    /** 角色 ID */
    private String roleid;
    /** 角色昵称 */
    private String rolename;
    /** 服务器 ID */
    private String serverid;
    /** 服务器名 */
    private String servername;
    /** 当前展示头像 id（通常是角色 id） */
    private String avatar;
    /** 角色等级 */
    private int lev;
    /** 世界等级 */
    @JsonProperty("worldlevel")
    private int worldLevel;
    /** 大亨等级；决定都市活力上限 */
    @JsonProperty("tycoonLevel")
    private int tycoonLevel;
    /** 角色累计活跃天数 */
    @JsonProperty("roleloginDays")
    private int roleLoginDays;
    /** 已获得角色数 */
    @JsonProperty("charidCnt")
    private int charidCnt;
    /** 本性像素当前值（体力） */
    @JsonProperty("staminaValue")
    private int staminaValue;
    /** 本性像素上限 */
    @JsonProperty("staminaMaxValue")
    private int staminaMaxValue;
    /** 都市活力当前值 */
    @JsonProperty("citystaminaValue")
    private int cityStaminaValue;
    /** 都市活力上限（受 tycoonLevel 影响） */
    @JsonProperty("citystaminaMaxValue")
    private int cityStaminaMaxValue;
    /** 今日活跃度（0–100） */
    @JsonProperty("dayvalue")
    private int dayValue;
    /** 周本剩余次数（封顶 3） */
    @JsonProperty("weekcopiesremainCnt")
    private int weekCopiesRemainCnt;
    /** 成就进度总览 */
    @JsonProperty("achieveProgress")
    private RoleHomeAchieveProgress achieveProgress;
    /** 区域进度列表 */
    @JsonProperty("areaProgress")
    private List<RoleHomeAreaProgress> areaProgress;
    /** 房产总览 */
    private RoleHomeRealEstate realestate;
    /** 载具总览 */
    private RoleHomeVehicle vehicle;
    /** 角色列表简版 */
    private List<RoleHomeCharacter> characters;

    public RoleHome() {}

    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }
    public String getRoleid() { return roleid; }
    public void setRoleid(String roleid) { this.roleid = roleid; }
    public String getRolename() { return rolename; }
    public void setRolename(String rolename) { this.rolename = rolename; }
    public String getServerid() { return serverid; }
    public void setServerid(String serverid) { this.serverid = serverid; }
    public String getServername() { return servername; }
    public void setServername(String servername) { this.servername = servername; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public int getLev() { return lev; }
    public void setLev(int lev) { this.lev = lev; }
    public int getWorldLevel() { return worldLevel; }
    public void setWorldLevel(int worldLevel) { this.worldLevel = worldLevel; }
    public int getTycoonLevel() { return tycoonLevel; }
    public void setTycoonLevel(int tycoonLevel) { this.tycoonLevel = tycoonLevel; }
    public int getRoleLoginDays() { return roleLoginDays; }
    public void setRoleLoginDays(int roleLoginDays) { this.roleLoginDays = roleLoginDays; }
    public int getCharidCnt() { return charidCnt; }
    public void setCharidCnt(int charidCnt) { this.charidCnt = charidCnt; }
    public int getStaminaValue() { return staminaValue; }
    public void setStaminaValue(int staminaValue) { this.staminaValue = staminaValue; }
    public int getStaminaMaxValue() { return staminaMaxValue; }
    public void setStaminaMaxValue(int staminaMaxValue) { this.staminaMaxValue = staminaMaxValue; }
    public int getCityStaminaValue() { return cityStaminaValue; }
    public void setCityStaminaValue(int cityStaminaValue) { this.cityStaminaValue = cityStaminaValue; }
    public int getCityStaminaMaxValue() { return cityStaminaMaxValue; }
    public void setCityStaminaMaxValue(int cityStaminaMaxValue) { this.cityStaminaMaxValue = cityStaminaMaxValue; }
    public int getDayValue() { return dayValue; }
    public void setDayValue(int dayValue) { this.dayValue = dayValue; }
    public int getWeekCopiesRemainCnt() { return weekCopiesRemainCnt; }
    public void setWeekCopiesRemainCnt(int weekCopiesRemainCnt) { this.weekCopiesRemainCnt = weekCopiesRemainCnt; }
    public RoleHomeAchieveProgress getAchieveProgress() { return achieveProgress; }
    public void setAchieveProgress(RoleHomeAchieveProgress achieveProgress) { this.achieveProgress = achieveProgress; }
    public List<RoleHomeAreaProgress> getAreaProgress() { return areaProgress; }
    public void setAreaProgress(List<RoleHomeAreaProgress> areaProgress) { this.areaProgress = areaProgress; }
    public RoleHomeRealEstate getRealestate() { return realestate; }
    public void setRealestate(RoleHomeRealEstate realestate) { this.realestate = realestate; }
    public RoleHomeVehicle getVehicle() { return vehicle; }
    public void setVehicle(RoleHomeVehicle vehicle) { this.vehicle = vehicle; }
    public List<RoleHomeCharacter> getCharacters() { return characters; }
    public void setCharacters(List<RoleHomeCharacter> characters) { this.characters = characters; }
}
