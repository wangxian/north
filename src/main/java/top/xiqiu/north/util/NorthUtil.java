package top.xiqiu.north.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
