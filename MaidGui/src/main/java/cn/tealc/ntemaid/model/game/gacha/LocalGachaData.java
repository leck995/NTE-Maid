package cn.tealc.ntemaid.model.game.gacha;

import cn.tealc.taygedo.model.GameGachaItem;

public class LocalGachaData extends GameGachaItem {
    private long id;
    private String roleId;
    private int gachaType; // DB存储int, getGachaType()转枚举

    public LocalGachaData() {}

    public LocalGachaData(String roleId, LocalGachaType gachaType) {
        this.roleId = roleId;
        this.gachaType = gachaType.getCode();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }

    public void setGachaType(int gachaType) { this.gachaType = gachaType; }
    public LocalGachaType getGachaType() { return LocalGachaType.fromCode(gachaType); }
}
