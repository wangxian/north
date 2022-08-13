package top.xiqiu.north.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

/**
 * north 工具类
 */
public class NorthUtil {
    /**
     * logger
     **/
    private static final Logger LOGGER = LoggerFactory.getLogger(NorthUtil.class);

    /**
     * 加载一个类
     *
     * @param fullPackageName 包名全路径，如：io.webapp.App
     * @return
     */
    public static Class<?> loadClass(String fullPackageName) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(fullPackageName);
        } catch (ClassNotFoundException e) {
            LOGGER.error("package class path loadClass error", e);
        }

        return null;
    }

    /**
     * 检查字符串 - 空 (null 或 长度为0)
     */
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    /**
     * 检查字符串 - 非空(!null 或 长度>0)
     */
    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    /**
     * 检查字符串是 - 空字符串
     * 是其中的一员：null, "", "   "
     */
    public static boolean isBlank(String s) {
        if (isEmpty(s)) {
            return true;
        }

        for (int i = 0, len = s.length(); i < len; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查字符串 - 非空
     * 不是其中的一种：null, "", "   "
     */
    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名，如 .txt, .jpg
     */
    public static String extName(String filename) {
        return isNotBlank(filename) ? filename.substring(filename.lastIndexOf(".")).toLowerCase() : "";
    }

    /**
     * 字符串计算 hash值
     *
     * @param str       字符串
     * @param algorithm 算法，支持: md5/sha1/sha256等
     * @return hash字符串（小写）
     */
    public static String hash(String str, String algorithm) {
        char[] hexChar = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            byte[] bytesInput = str.getBytes("UTF-8");

            MessageDigest messageDigest = MessageDigest.getInstance(algorithm.toLowerCase());
            messageDigest.update(bytesInput);

            byte[] digest = messageDigest.digest();
            int j = digest.length;
            char[] chars = new char[j * 2];

            int k = 0;
            for (int i = 0; i < j; ++i) {
                byte byte0 = digest[i];
                chars[k++] = hexChar[byte0 >>> 4 & 15];
                chars[k++] = hexChar[byte0 & 15];
            }

            return new String(chars);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 字符串计算md5
     */
    public static String md5(String str) {
        return hash(str, "md5");
    }

    /**
     * 字符串计算sha1
     */
    public static String sha1(String str) {
        return hash(str, "sha1");
    }
}
