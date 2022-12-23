package top.xiqiu.north.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * AES加密类
 */
public class AESUtils {
    private static final String CIPHER_NAME = "AES/CBC/PKCS5PADDING";

    /**
     * 128 bits
     */
    private static final int CIPHER_KEY_LEN = 16;

    /**
     * logger
     **/
    private static final Logger logger = LoggerFactory.getLogger(AESUtils.class);

    /**
     * AES 加密
     *
     * @param key 加密密钥
     * @param str 待加密字符串
     * @return 加密后字符串
     */
    @SuppressWarnings("CommentedOutCode")
    public static String encrypt(String key, String str) {
        key = paddingKey(key);

        String iv = key.substring(0, 6);
        iv += "30f7384ac2";

        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(UTF_8));
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(UTF_8), "AES");

            Cipher cipher = Cipher.getInstance(AESUtils.CIPHER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encryptedData = cipher.doFinal((str.getBytes(UTF_8)));

            // iv不再拼接到密文中
            // String encryptedDataInBase64 = Base64.getUrlEncoder().encodeToString(encryptedData);
            // String ivInBase64 = Base64.getUrlEncoder().encodeToString(iv.getBytes(UTF_8));
            // return ivInBase64 + encryptedDataInBase64;

            return Base64.getUrlEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            logger.error("AESUtils.encrypt 异常信息", e);
            // throw new RuntimeException(e);
            return null;
        }
    }

    /**
     * AES 解密
     *
     * @param key 解密密钥
     * @param str 待解密字符串
     * @return 解密后字符串
     */
    public static String decrypt(String key, String str) {
        key = paddingKey(key);

        String iv = key.substring(0, 6);
        iv += "30f7384ac2";

        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(UTF_8));
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(AESUtils.CIPHER_NAME);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decodedEncryptedData = Base64.getUrlDecoder().decode(str);
            byte[] original = cipher.doFinal(decodedEncryptedData);

            return new String(original);
        } catch (Exception e) {
            logger.error("AESUtils.decrypt 异常信息", e);
            // throw new RuntimeException(e);
            return null;
        }
    }

    /**
     * 填充key为需要的长度
     */
    private static String paddingKey(String key) {
        if (key.length() < AESUtils.CIPHER_KEY_LEN) {
            int numPad = AESUtils.CIPHER_KEY_LEN - key.length();

            // 0 pad to len 16 bytes
            key = key + "0".repeat(numPad);
        }

        if (key.length() > AESUtils.CIPHER_KEY_LEN) {
            // truncate to 16 bytes
            key = key.substring(0, CIPHER_KEY_LEN);
        }

        return key;
    }
}
