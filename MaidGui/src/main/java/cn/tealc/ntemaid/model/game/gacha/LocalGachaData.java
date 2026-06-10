package cn.tealc.ntemaid.model.game.gacha;

import java.util.List;

public class LocalGachaData {
    private String roleId;
    private String version;
    private long time;
    private int luckyType;//抽卡运气
    private List<LocalGachaPool> pools;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public List<LocalGachaPool> getPools() {
        return pools;
    }

    public void setPools(List<LocalGachaPool> pools) {
        this.pools = pools;
    }

    public int getLuckyType() {
        return luckyType;
    }

    public void setLuckyType(int luckyType) {
        this.luckyType = luckyType;
    }
}
