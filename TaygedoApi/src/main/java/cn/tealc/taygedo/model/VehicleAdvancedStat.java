package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 载具高级属性项。value/max 均为字符串形式，不做数值转换。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleAdvancedStat {
    /** 属性名 */
    private String name;
    /** 属性值（字符串） */
    private String value;
    /** 属性上限（字符串） */
    private String max;

    public VehicleAdvancedStat() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getMax() { return max; }
    public void setMax(String max) { this.max = max; }
}
