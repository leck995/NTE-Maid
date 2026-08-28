package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 各区域探索进度（含子项明细）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AreaProgress {
    /** 区域 ID */
    private String id;
    /** 区域名 */
    private String name;
    /** 已探索数 */
    private int progress;
    /** 可探索总数 */
    private int total;
    /** 子区域明细 */
    private List<AreaDetailItem> detail;

    public AreaProgress() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public List<AreaDetailItem> getDetail() { return detail; }
    public void setDetail(List<AreaDetailItem> detail) { this.detail = detail; }
}
