package cn.tealc.taygedo;

import cn.tealc.taygedo.model.LoginResult;
import cn.tealc.taygedo.model.UserCenterLoginResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Console;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TaygedoApi 验证码登录集成测试
 *
 * 该测试需要真实手机号接收短信验证码，通过命令行交互完成。
 *
 * 运行方式：
 * <pre>
 *   mvn -pl TaygedApi test -Dtest=TaygedoApiTest
 *   或指定手机号：
 *   mvn -pl TaygedApi test -Dtest=TaygedoApiTest -Dphone=13800138000
 * </pre>
 */
class TaygedoApiTest {
    private TaygedoApi api = new TaygedoApi();
    private static String deviceId = "0ce6d5b2c38a73dbc5e8b822b7a0c766";


    @Test
    @DisplayName("完整验证码登录流程：发送验证码 → 验证码登录 → 换取塔吉多令牌")
    void testSendCaptcha() {
        String phone = "17805101817";
        System.out.println("使用手机号: " + phone);
        System.out.println("\n[1/3] 发送短信验证码...");
        assertDoesNotThrow(() -> api.sendCaptcha(phone, deviceId),
                "发送验证码不应抛出异常");
        System.out.println("验证码发送成功，请查收短信");
    }


    @Test
    @DisplayName("完整验证码登录流程：发送验证码 → 验证码登录 → 换取塔吉多令牌")
    void testCaptchaLogin() {
        String phone = "17805101817";
        String captcha = "759573";

        // 第三步：验证码登录老虎平台
        System.out.println("\n[2/3] 验证码登录老虎平台...");
        LoginResult laohuResult = assertDoesNotThrow(
                () -> api.loginWithCaptcha(phone, captcha, deviceId),
                "验证码登录不应抛出异常");
        assertNotNull(laohuResult.getToken(), "老虎token不应为null");
        assertNotNull(laohuResult.getUserId(), "老虎userId不应为null");
        System.out.println("老虎登录成功");
        System.out.println("  token:  " + laohuResult.getToken().substring(0, 20) + "...");
        System.out.println("  userId: " + laohuResult.getUserId());

        // 第四步：换取塔吉多令牌
        System.out.println("\n[3/3] 换取塔吉多令牌...");
        UserCenterLoginResult tajiduoResult = assertDoesNotThrow(
                () -> api.userCenterLogin(laohuResult.getToken(), laohuResult.getUserId(), deviceId),
                "换取塔吉多令牌不应抛出异常");
        assertNotNull(tajiduoResult.getAccessToken(), "accessToken不应为null");
        assertNotNull(tajiduoResult.getRefreshToken(), "refreshToken不应为null");
        assertNotNull(tajiduoResult.getUid(), "uid不应为null");
        System.out.println("塔吉多登录成功");
        System.out.println("  accessToken:  " + tajiduoResult.getAccessToken().substring(0, 20) + "...");
        System.out.println("  refreshToken: " + tajiduoResult.getRefreshToken().substring(0, 20) + "...");
        System.out.println("  uid:          " + tajiduoResult.getUid());

        System.out.println("\n========== 登录流程全部完成 ==========");
    }

}
