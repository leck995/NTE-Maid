package cn.tealc.ntemaid.util.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * HT 加解密工具类
 * 提供文本多行加解密功能，支持三种预设密钥。
 */
public final class HTCryptoUtils {

    // ==================== 密钥定义 ====================

    public enum KeyId {
        Windows,
        WindowsCN,
        IOS
    }

    private static final class KnownKeys {
        static final byte[] Windows = Base64.getDecoder().decode("VVZiUDZwamp3NUtaaHZkZGllM3RmaGcxcFZra3ZlWTg=");
        static final byte[] WindowsCN = Base64.getDecoder().decode("MXpoNklPbElvaHJSODhVTlBqaUxpc3JrV0FDVVFZdXo=");
        static final byte[] IOS = Base64.getDecoder().decode("I2J3M0ZaFfHVO1Z8ZLIEcw==");

        static byte[] get(KeyId id) {
            switch (id) {
                case Windows: return Windows;
                case WindowsCN: return WindowsCN;
                case IOS: return IOS;
                default: throw new IllegalArgumentException("Unknown key");
            }
        }

        static final List<KeyId> AUTO_DETECT_ORDER = List.of(KeyId.Windows, KeyId.WindowsCN, KeyId.IOS);
    }

    // ==================== 单块加解密 ====================

    public static final class HTCipher {
        public static final String SPLIT_TOKEN = "|SPLIT|";
        public static final int BLOCK_SIZE = 16;

        public static byte[] encrypt(byte[] plaintext, KeyId keyId) throws GeneralSecurityException {
            SecretKeySpec keySpec = new SecretKeySpec(KnownKeys.get(keyId), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(plaintext);
        }

        public static byte[] decrypt(byte[] ciphertext, KeyId keyId) throws GeneralSecurityException {
            SecretKeySpec keySpec = new SecretKeySpec(KnownKeys.get(keyId), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(ciphertext);
        }

        public static boolean looksLikeBase64(String s) {
            if (s == null || s.length() < 4 || s.length() % 4 != 0) return false;
            return s.matches("^[A-Za-z0-9+/]+=*$");
        }

        public static DecryptLineResult tryDecryptBase64Line(String line) {
            if (!looksLikeBase64(line)) return null;
            byte[] cipher;
            try {
                cipher = Base64.getDecoder().decode(line);
            } catch (IllegalArgumentException e) {
                return null;
            }
            if (cipher.length == 0 || cipher.length % BLOCK_SIZE != 0) return null;

            for (KeyId keyId : KnownKeys.AUTO_DETECT_ORDER) {
                try {
                    byte[] plain = decrypt(cipher, keyId);
                    if (isValidUtf8(plain)) {
                        String text = new String(plain, StandardCharsets.UTF_8);
                        String expanded = text.replace(SPLIT_TOKEN, "\n");
                        expanded = expanded.replaceAll("[\r\n]+$", "");
                        return new DecryptLineResult(expanded, keyId);
                    }
                } catch (GeneralSecurityException ignored) {
                }
            }
            return null;
        }

        private static boolean isValidUtf8(byte[] bytes) {
            try {
                String s = new String(bytes, StandardCharsets.UTF_8);
                byte[] reencoded = s.getBytes(StandardCharsets.UTF_8);
                return Arrays.equals(bytes, reencoded);
            } catch (Exception e) {
                return false;
            }
        }

        public record DecryptLineResult(String text, KeyId usedKey) {}
    }

    // ==================== 多行文本处理 ====================

    public static final class HTLineProcessor {

        /**
         * 解密整个文本行列表（与C#版本逻辑完全一致）
         * @param lines 原始文件行列表（按行读入的文本）
         * @return 解密结果，包含合并后的文本及统计信息
         */
        public static DecryptResult decryptLines(List<String> lines) {
            StringBuilder sb = new StringBuilder();
            int decryptedBlocks = 0;
            int passthroughLines = 0;
            Set<KeyId> keysSeen = new HashSet<>();

            for (String rawLine : lines) {
                String trimmed = rawLine.trim();
                if (trimmed.isEmpty()) {
                    sb.append("\n");
                    continue;
                }

                var result = HTCipher.tryDecryptBase64Line(trimmed);
                if (result == null) {
                    sb.append(rawLine).append("\n");
                    passthroughLines++;
                    continue;
                }

                keysSeen.add(result.usedKey());
                String expanded = result.text();
                sb.append(expanded).append("\n");
                decryptedBlocks++;
            }

            return new DecryptResult(sb.toString(), decryptedBlocks, passthroughLines, keysSeen);
        }

        /**
         * 将明文加密为适合存储的多行 Base64 格式
         * @param plainText 原始文本（可包含任意换行符）
         * @param keyId 加密使用的密钥
         * @return 加密后的文本，每行对应原文本的一行，空行被转换为 SPLIT_TOKEN 的加密结果
         */
        public static String encryptLines(String plainText, KeyId keyId) throws GeneralSecurityException {
            String[] lines = plainText.split("\r?\n", -1);
            StringBuilder sb = new StringBuilder();
            byte[] splitTokenBytes = HTCipher.SPLIT_TOKEN.getBytes(StandardCharsets.UTF_8);

            for (String line : lines) {
                byte[] plaintext;
                if (line.isEmpty()) {
                    plaintext = splitTokenBytes;
                } else {
                    plaintext = line.getBytes(StandardCharsets.UTF_8);
                }
                byte[] cipher = HTCipher.encrypt(plaintext, keyId);
                sb.append(Base64.getEncoder().encodeToString(cipher)).append("\r\n");
            }
            return sb.toString();
        }

        public record DecryptResult(String text, int decryptedBlocks, int passthroughLines, Set<KeyId> keysUsed) {}
    }

    private HTCryptoUtils() {} // 禁止实例化
}