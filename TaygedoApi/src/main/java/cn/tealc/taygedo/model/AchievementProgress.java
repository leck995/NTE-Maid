package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 成就进度总览（已达成数/总数 + 金银铜牌数 + 分类明细）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AchievementProgress {
    /** 已达成成就数 */
    @JsonProperty("achievementCnt")
    private int achievementCnt;
    /** 成就总数 */
    private int total;
    /** 铜牌奖牌数 */
    @JsonProperty("bronzeUmdCnt")
    private int bronzeUmdCnt;
    /** 银牌奖牌数 */
    @JsonProperty("silverUmdCnt")
    private int silverUmdCnt;
    /** 金牌奖牌数 */
    @JsonProperty("goldUmdCnt")
    private int goldUmdCnt;
    /** 成就分类明细 */
    private List<AchievementCategory> detail;

    public AchievementProgress() {}

    public int getAchievementCnt() { return achievementCnt; }
    public void setAchievementCnt(int achievementCnt) { this.achievementCnt = achievementCnt; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getBronzeUmdCnt() { return bronzeUmdCnt; }
    public void setBronzeUmdCnt(int bronzeUmdCnt) { this.bronzeUmdCnt = bronzeUmdCnt; }
    public int getSilverUmdCnt() { return silverUmdCnt; }
    public void setSilverUmdCnt(int silverUmdCnt) { this.silverUmdCnt = silverUmdCnt; }
    public int getGoldUmdCnt() { return goldUmdCnt; }
    public void setGoldUmdCnt(int goldUmdCnt) { this.goldUmdCnt = goldUmdCnt; }
    public List<AchievementCategory> getDetail() { return detail; }
    public void setDetail(List<AchievementCategory> detail) { this.detail = detail; }
}
