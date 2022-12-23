package top.xiqiu.north.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Digest 工具
 */
public class DigestUtils {

    /**
     * 生成 MD5
     */
    public static String getMD5(String str) {
        return getHash("MD5", str);
    }

    /**
     * 生成 SHA1
     */
    public static String getSHA1(String str) {
        return getHash("SHA1", str);
    }

    /**
     * 生成 SHA256
     */
    public static String getSHA256(String str) {
        return getHash("SHA-256", str);
    }

    /**
     * 生成 HmacSHA1 签名
     */
    public static String getHmacSHA1(String str, String key) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");

            mac.init(signingKey);

            return byte2hex(mac.doFinal(str.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getHash(String algorithm, String str) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(str.getBytes(StandardCharsets.UTF_8));

            return byte2hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String byte2hex(byte[] byteDigest) {
        StringBuilder buf = new StringBuilder("");
        int i;

        for (int offset = 0; offset < byteDigest.length; offset++) {
            i = byteDigest[offset];

            if (i < 0) {
                i += 256;
            }

            if (i < 16) {
                buf.append("0");
            }

            buf.append(Integer.toHexString(i));
        }

        // 少1位
        // System.out.println("md5 = " + new BigInteger(1, byteDigest).toString(16));
        // System.out.println("md5 = " + buf.toString());

        return buf.toString();
    }
}
