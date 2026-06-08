package cn.tealc.taygedo;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.tealc.taygedo.TaygedoConstants.*;

/**
 * 塔吉多API加密与签名工具（package-private，仅供同包内使用）
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li><b>DS签名</b>：用于Native请求头，格式 {timestamp},{nonce},{MD5(timestamp+nonce+appVer+dsSecret)}</li>
 *   <li><b>老虎平台签名</b>：将参数按key排序后拼接value，追加secret后计算MD5</li>
 *   <li><b>AES加密</b>：AES-128-ECB/PKCS5Padding，密钥取自LAOHU_SECRET后16字符</li>
 * </ul>
 */
final class TaygedoCrypto {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TaygedoCrypto() {}

    /**
     * 生成DS签名
     * 格式：{timestamp},{nonce},{MD5(timestamp + nonce + appVer + dsSecret)}
     */
    static String makeDs() {
        long timestamp = System.currentTimeMillis() / 1000;
        String nonce = makeNonce();
        String raw = timestamp + nonce + TAYGEDO_APP_VER + TAYGEDO_DS_SECRET;
        String sign = org.apache.commons.codec.digest.DigestUtils.md5Hex(raw);
        return timestamp + "," + nonce + "," + sign;
    }

    /** 生成8位随机nonce，使用拒绝采样避免模偏差 */
    private static String makeNonce() {
        int alphabetLen = NONCE_ALPHABET.length();
        int fairRange = (256 / alphabetLen) * alphabetLen;
        StringBuilder nonce = new StringBuilder(8);
        byte[] bytes = new byte[8];
        while (nonce.length() < 8) {
            SECURE_RANDOM.nextBytes(bytes);
            for (byte b : bytes) {
                int unsigned = b & 0xFF;
                if (unsigned < fairRange) {
                    nonce.append(NONCE_ALPHABET.charAt(unsigned % alphabetLen));
                    if (nonce.length() == 8) break;
                }
            }
        }
        return nonce.toString();
    }

    /**
     * 老虎平台请求签名
     * 将参数按key排序后拼接所有value，再追加LAOHU_SECRET，计算MD5
     */
    static String laohuSign(Map<String, String> data) {
        String values = data.keySet().stream()
                .sorted()
                .map(data::get)
                .collect(Collectors.joining());
        return org.apache.commons.codec.digest.DigestUtils.md5Hex(values + LAOHU_SECRET);
    }

    /** 构建带签名的老虎请求体：先签名，再将sign加入参数，然后form编码 */
    static String signedLaohuBody(Map<String, String> data) {
        Map<String, String> withSign = new LinkedHashMap<>(data);
        withSign.put("sign", laohuSign(data));
        return TaygedoHttpClient.formEncode(withSign);
    }

    /**
     * AES-128-ECB加密并Base64编码
     * 密钥为LAOHU_SECRET的后16个字符，用于加密手机号和验证码
     */
    static String aesBase64Encode(String value) {
        try {
            String keyStr = LAOHU_SECRET.substring(LAOHU_SECRET.length() - AES_KEY_LENGTH);
            SecretKeySpec key = new SecretKeySpec(keyStr.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new TaygedoException("AES 加密失败: " + e.getMessage(), e);
        }
    }
}
