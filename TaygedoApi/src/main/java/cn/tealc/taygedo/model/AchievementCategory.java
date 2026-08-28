package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 成就分类明细
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AchievementCategory {
    /** 分类 ID */
    private String id;
    /** 分类名 */
    private String name;
    /** 该分类已达成数 */
    private int progress;
    /** 该分类总数 */
    private int total;

    public AchievementCategory() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
