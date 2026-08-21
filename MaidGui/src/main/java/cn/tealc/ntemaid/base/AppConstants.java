package cn.tealc.ntemaid.base;

public final class AppConstants {
    private AppConstants() {}
    public static final String VERSION = "1.2.3";
    public static final String APP_AUTHOR = "Leck";
    public static final String URL_APP_UPDATE = "https://cdn.999758.xyz/nte/release.json";
    public static final String URL_APP_UPDATE_DEV = "https://cdn.999758.xyz/nte/release-dev.json";
    public static final String URL_GITHUB = "https://github.com/leck995/NTE-Maid";
    public static final String URL_GITHUB_ISSUES = URL_GITHUB + "/issues";
    public static final String URL_SUPPORT_LIST = "";
    public static final String URL_APP_WIKI = "";
    public static final String URL_WIKI_GACHA_GET =  "https://www.yuque.com/chashuisuipian/mstwd1/avch2nlqpla52fp0";

    public static final String URL_HOST_SERVER = "https://api.999758.xyz:20141";
    //public static final String URL_HOST_SERVER = "http://127.0.0.1:8080";
    public static final String URL_REDEMPTION_CODES = URL_HOST_SERVER + "/api/redemption-codes/mc";
    public static final String URL_ANNOUNCEMENTS = URL_HOST_SERVER +  "/api/announcements/game";
    public static final String URL_AUTH_LOGIN = URL_HOST_SERVER +  "/api/auth/login";
    public static final String URL_AUTH_VERIFY = URL_HOST_SERVER +  "/api/auth/verify";
    public static final String URL_FILE_UPLOAD = URL_HOST_SERVER +  "/api/mc/gacha/uploadByUser";
    public static final String URL_GACHA_FILE_LIST = URL_HOST_SERVER + "/api/mc/gacha/listByUser";
    public static final String URL_GACHA_FILE_DELETE = URL_HOST_SERVER + "/api/mc/gacha/deleteByUser/";


}
