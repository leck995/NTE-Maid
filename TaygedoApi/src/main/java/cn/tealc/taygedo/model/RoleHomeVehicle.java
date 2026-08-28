package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 角色综合面板中的载具总览
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleHomeVehicle {
    /** 拥有载具数 */
    @JsonProperty("ownCnt")
    private int ownCnt;
    /** 当前展示载具 id（用于出图） */
    @JsonProperty("showId")
    private String showId;
    /** 当前展示载具名 */
    @JsonProperty("showName")
    private String showName;
    /** 可获得载具总数 */
    private int total;

    public RoleHomeVehicle() {}

    public int getOwnCnt() { return ownCnt; }
    public void setOwnCnt(int ownCnt) { this.ownCnt = ownCnt; }
    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }
    public String getShowName() { return showName; }
    public void setShowName(String showName) { this.showName = showName; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
