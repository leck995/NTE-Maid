package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 角色详细列表元素（每个角色 15+ 属性 + 城市技能 + 副手弧盘/驱动盘套装）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterDetail {
    /** 角色 ID */
    private String id;
    /** 角色名 */
    private String name;
    /** 角色等级 */
    private int alev;
    /** 混频等级 */
    private int slev;
    /** 好感度（羁遇）累计经验值 */
    @JsonProperty("likeabilitylev")
    private int likeabilityLev;
    /** 觉醒等级 */
    @JsonProperty("awakenLev")
    private int awakenLev;
    /** 已激活的觉醒效果列表 */
    @JsonProperty("awakenEffect")
    private List<String> awakenEffect;
    /** 元素类型（原始字符串，如 CHARACTER_ELEMENT_TYPE_PSYCHE） */
    @JsonProperty("elementType")
    private String elementType;
    /** 组别/命途（原始字符串，如 CHARACTER_GROUP_TYPE_ONE） */
    @JsonProperty("groupType")
    private String groupType;
    /** 品质（原始字符串，如 ITEM_QUALITY_ORANGE） */
    private String quality;
    /** 角色属性面板项 */
    private List<CharacterProperty> properties;
    /** 战技列表 */
    private List<CharacterSkill> skills;
    /** 城区技能列表 */
    @JsonProperty("citySkills")
    private List<CharacterSkill> citySkills;
    /** 弧盘/武器 */
    private CharacterFork fork;
    /** 驱动盘套装 */
    private CharacterSuit suit;

    public CharacterDetail() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAlev() { return alev; }
    public void setAlev(int alev) { this.alev = alev; }
    public int getSlev() { return slev; }
    public void setSlev(int slev) { this.slev = slev; }
    public int getLikeabilityLev() { return likeabilityLev; }
    public void setLikeabilityLev(int likeabilityLev) { this.likeabilityLev = likeabilityLev; }
    public int getAwakenLev() { return awakenLev; }
    public void setAwakenLev(int awakenLev) { this.awakenLev = awakenLev; }
    public List<String> getAwakenEffect() { return awakenEffect; }
    public void setAwakenEffect(List<String> awakenEffect) { this.awakenEffect = awakenEffect; }
    public String getElementType() { return elementType; }
    public void setElementType(String elementType) { this.elementType = elementType; }
    public String getGroupType() { return groupType; }
    public void setGroupType(String groupType) { this.groupType = groupType; }
    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }
    public List<CharacterProperty> getProperties() { return properties; }
    public void setProperties(List<CharacterProperty> properties) { this.properties = properties; }
    public List<CharacterSkill> getSkills() { return skills; }
    public void setSkills(List<CharacterSkill> skills) { this.skills = skills; }
    public List<CharacterSkill> getCitySkills() { return citySkills; }
    public void setCitySkills(List<CharacterSkill> citySkills) { this.citySkills = citySkills; }
    public CharacterFork getFork() { return fork; }
    public void setFork(CharacterFork fork) { this.fork = fork; }
    public CharacterSuit getSuit() { return suit; }
    public void setSuit(CharacterSuit suit) { this.suit = suit; }
}
