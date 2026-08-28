package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 载具基础属性项。value 为字符串形式，不做数值转换。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleBaseStat {
    /** 属性名 */
    private String name;
    /** 属性值（字符串，如 "146"、"18000"） */
    private String value;

    public VehicleBaseStat() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
