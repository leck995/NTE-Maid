package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 载具装饰/涂装子条目。
 * <p>type 才是 {CDN}/verhicle/model/{type}.png 的 id。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleModel {
    /** 条目 ID */
    private String id;
    /** 涂装类型 ID（出图用，走 CDN verhicle/model/{type}.png） */
    private String type;

    public VehicleModel() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
