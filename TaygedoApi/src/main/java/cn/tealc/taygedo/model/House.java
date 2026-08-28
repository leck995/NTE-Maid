package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 角色拥有的房产。
 * <p>chars 是居住角色 id 列表的 JSON 字符串（如 "[1019]"），缺省房源为空串，
 * 调用方可按需自行二次反序列化。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class House {
    /** 房产 ID */
    private String id;
    /** 房产名 */
    private String name;
    /** 是否拥有 */
    private boolean own;
    /** 居住角色 id 列表的 JSON 字符串，如 "[1019]"；缺省房源为空串 */
    private String chars;
    /** 家具明细 */
    private List<Furniture> fdetail;

    public House() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isOwn() { return own; }
    public void setOwn(boolean own) { this.own = own; }
    public String getChars() { return chars; }
    public void setChars(String chars) { this.chars = chars; }
    public List<Furniture> getFdetail() { return fdetail; }
    public void setFdetail(List<Furniture> fdetail) { this.fdetail = fdetail; }
}
