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
}
