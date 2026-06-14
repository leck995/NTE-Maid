package cn.tealc.ntemaid.model.game;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Weapon {
    private String id;
    private String zh;
    private String ja;
    private String ko;
    private int rarity;
    private String icon;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getZh() {
        return zh;
    }

    public void setZh(String zh) {
        this.zh = zh;
    }

    public String getJa() {
        return ja;
    }

    public void setJa(String ja) {
        this.ja = ja;
    }

    public String getKo() {
        return ko;
    }

    public void setKo(String ko) {
        this.ko = ko;
    }

    public int getRarity() {
        return rarity;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
