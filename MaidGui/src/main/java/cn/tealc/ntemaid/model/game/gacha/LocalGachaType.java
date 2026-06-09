package cn.tealc.ntemaid.model.game.gacha;

public enum LocalGachaType {
    UP_ROLE_POOL(0), DEFAULT_ROLE_POOL(1), WEAPON_POOL(2),UNKNOWN(-1);
    private final int code;

    LocalGachaType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static LocalGachaType fromCode(int code) {
        for (LocalGachaType t : values()) {
            if (t.code == code) return t;
        }
        return DEFAULT_ROLE_POOL;
    }
}
