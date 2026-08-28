package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 技能子项
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterSkillItem {
    /** 技能项标题 */
    private String title;
    /** 技能项描述 */
    private String desc;

    public CharacterSkillItem() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
}
