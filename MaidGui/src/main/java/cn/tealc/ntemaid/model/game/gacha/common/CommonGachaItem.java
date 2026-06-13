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
    private int rollPoints;

    @JsonProperty("roll_label")
    private String rollLabel;

    private String playerId;

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

    public int getRollPoints() {
        return rollPoints;
    }

    public void setRollPoints(int rollPoints) {
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
