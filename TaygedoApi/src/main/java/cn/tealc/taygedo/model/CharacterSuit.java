package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 驱动盘套装
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterSuit {
    /** 套装 ID */
    private String id;
    /** 套装名 */
    private String name;
    /** 2 件套描述 */
    private String des2;
    /** 4 件套描述 */
    private String des4;
    /** 套装触发条件 */
    @JsonProperty("suitCondition")
    private List<String> suitCondition;
    /** 核心件列表 */
    private List<CharacterSuitItem> core;
    /** 散件列表 */
    private List<CharacterSuitItem> pie;
    /** 已激活件数 */
    @JsonProperty("suitActivateNum")
    private int suitActivateNum;

    public CharacterSuit() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDes2() { return des2; }
    public void setDes2(String des2) { this.des2 = des2; }
    public String getDes4() { return des4; }
    public void setDes4(String des4) { this.des4 = des4; }
    public List<String> getSuitCondition() { return suitCondition; }
    public void setSuitCondition(List<String> suitCondition) { this.suitCondition = suitCondition; }
    public List<CharacterSuitItem> getCore() { return core; }
    public void setCore(List<CharacterSuitItem> core) { this.core = core; }
    public List<CharacterSuitItem> getPie() { return pie; }
    public void setPie(List<CharacterSuitItem> pie) { this.pie = pie; }
    public int getSuitActivateNum() { return suitActivateNum; }
    public void setSuitActivateNum(int suitActivateNum) { this.suitActivateNum = suitActivateNum; }
}
