package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 家具明细
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Furniture {
    /** 家具 ID */
    private String id;
    /** 家具名 */
    private String name;
    /** 是否拥有 */
    private boolean own;

    public Furniture() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isOwn() { return own; }
    public void setOwn(boolean own) { this.own = own; }
}
