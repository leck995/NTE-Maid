package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 房产数据 wrapper，包含房产列表与汇总信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RealEstateResult {
    /** 房产列表 */
    private List<House> detail;
    /** 拥有房产数 */
    @JsonProperty("ownCnt")
    private int ownCnt;
    /** 当前展示房产 id */
    @JsonProperty("showId")
    private String showId;
    /** 当前展示房产名 */
    @JsonProperty("showName")
    private String showName;
    /** 可获得房产总数 */
    private int total;

    public RealEstateResult() {}

    public List<House> getDetail() { return detail; }
    public void setDetail(List<House> detail) { this.detail = detail; }
    public int getOwnCnt() { return ownCnt; }
    public void setOwnCnt(int ownCnt) { this.ownCnt = ownCnt; }
    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }
    public String getShowName() { return showName; }
    public void setShowName(String showName) { this.showName = showName; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
