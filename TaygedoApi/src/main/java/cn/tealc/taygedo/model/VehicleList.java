package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 载具数据 wrapper，包含载具列表与汇总信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleList {
    /** 载具列表 */
    private List<Vehicle> detail;
    /** 拥有载具数 */
    @JsonProperty("ownCnt")
    private int ownCnt;
    /** 当前展示载具 id */
    @JsonProperty("showId")
    private String showId;
    /** 当前展示载具名 */
    @JsonProperty("showName")
    private String showName;
    /** 可获得载具总数 */
    private int total;

    public VehicleList() {}

    public List<Vehicle> getDetail() { return detail; }
    public void setDetail(List<Vehicle> detail) { this.detail = detail; }
    public int getOwnCnt() { return ownCnt; }
    public void setOwnCnt(int ownCnt) { this.ownCnt = ownCnt; }
    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }
    public String getShowName() { return showName; }
    public void setShowName(String showName) { this.showName = showName; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
