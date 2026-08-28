package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 角色属性面板项；部分驱动盘 main_properties 项服务端只回 id，缺 name/value，故字段均可为空
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterProperty {
    /** 属性 ID */
    private String id;
    /** 属性名 */
    private String name;
    /** 属性值（字符串形式） */
    private String value;

    public CharacterProperty() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
