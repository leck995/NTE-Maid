package cn.tealc.ntemaid.model.game.music;


import java.net.URI;
import java.nio.file.Path;

public class Music {
    private Integer id;
    private String title;
    private String artist;
    private String album;
    private Integer duration; // 秒
    private String filePath;//路径
    private Long addTime;

    public Music() {
    }


    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public Integer getDuration() {
        return duration;
    }

    public Long getAddTime() {
        return addTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setAddTime(Long addTime) {
        this.addTime = addTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Music music) {
            return music.filePath.equals(this.filePath);
        }
        return super.equals(obj);
    }

    public URI getUri() {
        if (filePath == null) return null;
        return Path.of(filePath).toUri();
    }

}
