package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 角色综合面板中的区域进度项
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleHomeAreaProgress {
    /** 区域 ID */
    private String id;
    /** 区域名 */
    private String name;
    /** 该地区已探索数 */
    private int progress;
    /** 该地区可探索总数 */
    private int total;

    public RoleHomeAreaProgress() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
