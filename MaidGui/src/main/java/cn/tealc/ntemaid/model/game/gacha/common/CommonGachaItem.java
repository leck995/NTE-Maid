package cn.tealc.ntemaid.model.game.gacha.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonGachaItem {
    @JsonProperty("record_id")
    private String recordId;

    @JsonProperty("record_type")
    private String recordType;

    @JsonProperty("time")
    private String time;  // 保持为 String，也可转换为 LocalDateTime

    @JsonProperty("pool_id")
    private String poolId;

    @JsonProperty("pool_name")
    private String poolName;

    @JsonProperty("item_id")
    private String itemId;

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("count")
    private int count;

    @JsonProperty("roll_points")
    private long rollPoints;

    @JsonProperty("roll_label")
    private String rollLabel;

    private String playerId;

    private int upCount; //获取到该物品的总抽数
    private int rarity; //物品等级
    private boolean up;
    private long id;
    private int sort;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getUpCount() {
        return upCount;
    }

    public void setUpCount(int upCount) {
        this.upCount = upCount;
    }

    public int getRarity() {
        return rarity;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public CommonGachaItem() {
    }


    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPoolId() {
        return poolId;
    }

    public void setPoolId(String poolId) {
        this.poolId = poolId;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getRollPoints() {
        return rollPoints;
    }

    public void setRollPoints(long rollPoints) {
        this.rollPoints = rollPoints;
    }

    public String getRollLabel() {
        return rollLabel;
    }

    public void setRollLabel(String rollLabel) {
        this.rollLabel = rollLabel;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    @Override
    public String toString() {
        return "MonopolyRecord{" +
                "recordId='" + recordId + '\'' +
                ", recordType='" + recordType + '\'' +
                ", time='" + time + '\'' +
                ", poolId='" + poolId + '\'' +
                ", poolName='" + poolName + '\'' +
                ", itemId='" + itemId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", count=" + count +
                ", rollPoints=" + rollPoints +
                ", rollLabel='" + rollLabel + '\'' +
                '}';
    }

}
