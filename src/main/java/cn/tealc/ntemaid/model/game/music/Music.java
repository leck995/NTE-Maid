package cn.tealc.ntemaid.model.game.music;


import java.net.URI;
import java.nio.file.Path;

public class Music {
    private String name;
    private String url;//路径

    public Music() {
    }

    public Music(String url, String name) {
        this.url = url;
        if (name.contains(".")){
            this.name = name.substring(0, name.lastIndexOf("."));
        }else {
            this.name = name;
        }

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Music music) {
            return music.url.equals(this.url);
        }
        return super.equals(obj);
    }

    public URI getUri() {
        if (url == null) return null;
        return Path.of(url).toUri();
    }

}
