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

    public static LocalGachaType fromName(String name) {
        return switch (name){
            case "限定卡池" -> LocalGachaType.UP_ROLE_POOL;
            case "常驻卡池" -> LocalGachaType.DEFAULT_ROLE_POOL;
            case "弧盘池" -> LocalGachaType.WEAPON_POOL;
            default -> LocalGachaType.UNKNOWN;
        };
    }
}
