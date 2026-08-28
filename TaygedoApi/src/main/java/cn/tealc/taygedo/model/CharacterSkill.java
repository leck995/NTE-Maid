package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 角色技能（战技或城区技能）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterSkill {
    /** 技能 ID */
    private String id;
    /** 技能名 */
    private String name;
    /** 技能类型 */
    private String type;
    /** 技能等级 */
    private int level;
    /** 技能子项列表 */
    private List<CharacterSkillItem> items;

    public CharacterSkill() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public List<CharacterSkillItem> getItems() { return items; }
    public void setItems(List<CharacterSkillItem> items) { this.items = items; }
}
