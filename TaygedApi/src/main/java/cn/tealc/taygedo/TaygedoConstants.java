package cn.tealc.taygedo;

/**
 * 塔吉多API常量定义
 * 包含API基础URL、版本号、签名密钥、User-Agent、游戏ID列表等所有配置常量
 */
class TaygedoConstants {
    /** 塔吉多API基础地址 */
    static final String TAYGEDO_BASE_URL = "https://bbs-api.tajiduo.com";
    /** 塔吉多App版本号，用于请求头 */
    static final String TAYGEDO_APP_VER = "1.2.2";
    /** DS签名密钥，用于生成Native请求的ds头 */
    static final String TAYGEDO_DS_SECRET = "pUds3dfMkl";
    /** H5请求的Origin和Referer地址 */
    static final String H5_ORIGIN = "https://webstatic.tajiduo.com";

    /** 老虎用户中心API基础地址，用于短信验证码和手机号登录 */
    static final String LAOHU_BASE_URL = "https://user.laohu.com";
    /** 老虎用户中心密钥，用于请求签名和AES加密 */
    static final String LAOHU_SECRET = "89155cc4e8634ec5b1b6364013b23e3e";

    /** 模拟iOS原生App的User-Agent */
    static final String NATIVE_USER_AGENT = "Tajiduo/1.2.2 (iPhone; iOS 17.0; Scale/3.00)";
    /** 模拟H5内嵌WebView的User-Agent */
    static final String H5_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 Tajiduo/1.2.2";

    /** 随机nonce字符集，包含大小写字母和数字 */
    static final String NONCE_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /** 已知的游戏ID列表，签到时会遍历这些游戏 */
    public static final String[] GAME_IDS = {"1256", "1257", "1289"};

    /** AES密钥长度（128位 = 16字节） */
    static final int AES_KEY_LENGTH = 16;

    private TaygedoConstants() {
    }
}
