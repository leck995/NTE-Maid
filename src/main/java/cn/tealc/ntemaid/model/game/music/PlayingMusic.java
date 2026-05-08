package cn.tealc.ntemaid.model.game.music;

public class PlayingMusic {
    private Integer id;
    private Integer musicId;
    private Integer sortOrder;
    
    // 关联的歌曲详情，方便 UI 直接显示
    private Music music;

    public PlayingMusic() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMusicId() {
        return musicId;
    }

    public void setMusicId(Integer musicId) {
        this.musicId = musicId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }
}