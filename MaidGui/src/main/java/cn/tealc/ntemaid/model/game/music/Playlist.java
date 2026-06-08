package cn.tealc.ntemaid.model.game.music;

import java.util.List;

public class Playlist {
    private Integer id;
    private String name;
    private String description;
    private String coverPath;
    private Long createTime;
    private String type;//预留字段，用于后续开发车俩单独歌单设计
    
    // 非数据库字段：用于业务逻辑，方便一次性获取歌单下的所有歌曲
    private List<Music> songs;

    public Playlist() {}

    // Getter and Setter ...
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }
    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }
    public List<Music> getSongs() { return songs; }
    public void setSongs(List<Music> songs) { this.songs = songs; }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}