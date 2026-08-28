package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 区域子项明细。progress 使用 Integer 包装类型：服务端未开启/未解锁时返回 null。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AreaDetailItem {
    /** 子项 ID */
    private String id;
    /** 子项名 */
    private String name;
    /** 子项总数 */
    private int total;
    /** 进度；未开启/未解锁时服务端返回 null */
    private Integer progress;

    public AreaDetailItem() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
}
