package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 单个载具（含基础/进阶属性、涂装模型）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vehicle {
    /** 载具 ID */
    private String id;
    /** 载具名 */
    private String name;
    /** 是否拥有 */
    private boolean own;
    /** 基础属性列表 */
    private List<VehicleBaseStat> base;
    /** 高级属性列表 */
    private List<VehicleAdvancedStat> advanced;
    /** 装饰/涂装子条目列表 */
    private List<VehicleModel> models;

    public Vehicle() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isOwn() { return own; }
    public void setOwn(boolean own) { this.own = own; }
    public List<VehicleBaseStat> getBase() { return base; }
    public void setBase(List<VehicleBaseStat> base) { this.base = base; }
    public List<VehicleAdvancedStat> getAdvanced() { return advanced; }
    public void setAdvanced(List<VehicleAdvancedStat> advanced) { this.advanced = advanced; }
    public List<VehicleModel> getModels() { return models; }
    public void setModels(List<VehicleModel> models) { this.models = models; }
}
