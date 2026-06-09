package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 抽卡数据，包含玩家信息和各卡池抽卡记录
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameGachaResult {
    private String avatar;
    private int lev;
    private String luckTitle;
    private int luckType;
    private String roleid;
    private String rolename;
    private String userid;
    private List<GameGachaPool> gachaDetails;

    public GameGachaResult() {}

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public int getLev() { return lev; }
    public void setLev(int lev) { this.lev = lev; }
    public String getLuckTitle() { return luckTitle; }
    public void setLuckTitle(String luckTitle) { this.luckTitle = luckTitle; }
    public int getLuckType() { return luckType; }
    public void setLuckType(int luckType) { this.luckType = luckType; }
    public String getRoleid() { return roleid; }
    public void setRoleid(String roleid) { this.roleid = roleid; }
    public String getRolename() { return rolename; }
    public void setRolename(String rolename) { this.rolename = rolename; }
    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }
    public List<GameGachaPool> getGachaDetails() { return gachaDetails; }
    public void setGachaDetails(List<GameGachaPool> gachaDetails) { this.gachaDetails = gachaDetails; }
}
