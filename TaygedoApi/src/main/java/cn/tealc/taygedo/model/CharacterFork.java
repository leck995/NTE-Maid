package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 角色弧盘/武器。
 * <p>name 是弧盘显示名（如"预备备"），buffName 是绑定 Buff 名（如"「司令虎符」"）。
 * id 形如 fork_&lt;拼音&gt;，走 {CDN}/character/fork/{id}.png 出图；未持有时 id 为空串。
 * <p>注意：alev/blev/slev 在此对象中是字符串类型（服务端返回字符串）。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterFork {
    /** 弧盘 ID（fork_*） */
    private String id;
    /** 弧盘显示名 */
    private String name;
    /** 武器等级（字符串） */
    private String alev;
    /** 突破阶数（右侧星数，字符串） */
    private String blev;
    /** 混频等级（精炼/共鸣阶，字符串） */
    private String slev;
    /** 弧盘品质（原始字符串，如 ITEM_QUALITY_ORANGE） */
    private String quality;
    /** 命途/组别（原始字符串，如 CHARACTER_GROUP_TYPE_ONE） */
    private String groupType;
    /** 弧盘背景文案 / 故事描述 */
    private String des;
    /** 绑定 Buff 名，如「司令虎符」 */
    private String buffName;
    /** Buff 描述模板，含 <lv>{N}</> 占位符，需与 lbd 联合渲染 */
    private String buffDes;
    /** 等级数值表，按下标替换 buffDes 的 {N} 占位符 */
    private List<String> lbd;
    /** 武器面板属性（基础攻击力 / 副词条） */
    private List<CharacterProperty> properties;

    public CharacterFork() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAlev() { return alev; }
    public void setAlev(String alev) { this.alev = alev; }
    public String getBlev() { return blev; }
    public void setBlev(String blev) { this.blev = blev; }
    public String getSlev() { return slev; }
    public void setSlev(String slev) { this.slev = slev; }
    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }
    public String getGroupType() { return groupType; }
    public void setGroupType(String groupType) { this.groupType = groupType; }
    public String getDes() { return des; }
    public void setDes(String des) { this.des = des; }
    public String getBuffName() { return buffName; }
    public void setBuffName(String buffName) { this.buffName = buffName; }
    public String getBuffDes() { return buffDes; }
    public void setBuffDes(String buffDes) { this.buffDes = buffDes; }
    public List<String> getLbd() { return lbd; }
    public void setLbd(List<String> lbd) { this.lbd = lbd; }
    public List<CharacterProperty> getProperties() { return properties; }
    public void setProperties(List<CharacterProperty> properties) { this.properties = properties; }
}
