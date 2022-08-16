package top.xiqiu.north.core;

import top.xiqiu.north.util.NorthUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;

public class AppConfig extends Properties {
    /**
     * 单例对象
     */
    private static AppConfig _appConfig;

    /**
     * app version
     */
    private String northVersion = "";

    /**
     * 初始化配置（单例）
     */
    public static AppConfig init() {
        if (_appConfig == null) {
            _appConfig = new AppConfig();

            // 加载系统 properties 配置
            System.getProperties().forEach((key, value) -> _appConfig.setProperty(String.valueOf(key), String.valueOf(value)));

            // 读取 application.properties 配置文件
            try (InputStream resourceAsStream = _appConfig.getClass().getClassLoader().getResourceAsStream("application.properties")) {
                if (resourceAsStream != null) {
                    _appConfig.load(resourceAsStream);
                }
            } catch (IOException e) {
            }

            // 获取north当前版本号 - 注意：只有在fatjar下生效
            try (InputStream resourceAsStream = _appConfig.getClass().getClassLoader().getResourceAsStream("META-INF/maven/top.xiqiu/north/pom.properties")) {
                if (resourceAsStream != null) {
                    Properties northPkgInfo = new Properties();
                    northPkgInfo.load(resourceAsStream);
                    _appConfig.northVersion = northPkgInfo.getProperty("version");
                }
            } catch (IOException e) {
            }


            // 加载系统 env 环境变量
            // 优先级：环境变量 > application.properties > Java系统属性
            System.getenv().forEach((key, value) -> _appConfig.setProperty(key.toLowerCase(), value));
        }

        return _appConfig;
    }

    /**
     * 获取配置 - 字符串
     *
     * @param key          键值
     * @param defaultValue 默认值
     */
    public String get(String key, String defaultValue) {
        return getProperty(key, defaultValue);
    }

    /**
     * 获取配置 - 整型
     *
     * @param key          键值
     * @param defaultValue 默认值
     */
    public int getInt(String key, int defaultValue) {
        return getOrDefault(key, defaultValue, Integer::parseInt);
    }

    /**
     * 获取配置 - 单精度
     *
     * @param key          键值
     * @param defaultValue 默认值
     */
    public float getFloat(String key, float defaultValue) {
        return getOrDefault(key, defaultValue, Float::parseFloat);
    }

    /**
     * 获取配置 - 双精度
     *
     * @param key          键值
     * @param defaultValue 默认值
     */
    public double getDouble(String key, double defaultValue) {
        return getOrDefault(key, defaultValue, Double::parseDouble);
    }

    /**
     * 获取配置 - 长整型
     *
     * @param key          键值
     * @param defaultValue 默认值
     */
    public long getLong(String key, long defaultValue) {
        return getOrDefault(key, defaultValue, Long::parseLong);
    }

    /**
     * 获取配置 基本方法
     */
    private <T> T getOrDefault(String key, T defaultValue, Function<String, T> convert) {
        String temp = getProperty(key);
        if (NorthUtil.isEmpty(temp)) {
            return defaultValue;
        } else {
            return convert.apply(temp);
        }
    }

    /**
     * 获取 north 版本号（如：1.0.2)
     */
    public String getNorthVersion() {
        return northVersion;
    }
}
