package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 单件驱动盘
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterSuitItem {
    /** 盘件 ID */
    private String id;
    /** 盘件名 */
    private String name;
    /** 盘件等级 */
    private int lev;
    /** 主词条 */
    @JsonProperty("mainProperties")
    private List<CharacterProperty> mainProperties;
    /** 副词条 */
    private List<CharacterProperty> properties;

    public CharacterSuitItem() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getLev() { return lev; }
    public void setLev(int lev) { this.lev = lev; }
    public List<CharacterProperty> getMainProperties() { return mainProperties; }
    public void setMainProperties(List<CharacterProperty> mainProperties) { this.mainProperties = mainProperties; }
    public List<CharacterProperty> getProperties() { return properties; }
    public void setProperties(List<CharacterProperty> properties) { this.properties = properties; }
}
