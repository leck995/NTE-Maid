package cn.tealc.ntemaid.model.game.gacha.local;

import cn.tealc.taygedo.model.GameGachaItem;

public class LocalGachaItem extends GameGachaItem {
    private long id;
    private String roleId;
    private int gachaType; // DB存储int, getGachaType()转枚举
    private boolean up; //是否是限定
    private boolean upReallyCount; //实际Up抽数


    public LocalGachaItem() {
    }


    public LocalGachaItem(String roleId, LocalGachaType gachaType) {
        this.roleId = roleId;
        this.gachaType = gachaType.getCode();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public void setGachaType(int gachaType) {
        this.gachaType = gachaType;
    }

    public LocalGachaType getGachaType() {
        return LocalGachaType.fromCode(gachaType);
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public boolean isUpReallyCount() {
        return upReallyCount;
    }

    public void setUpReallyCount(boolean upReallyCount) {
        this.upReallyCount = upReallyCount;
    }
}
