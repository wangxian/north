package top.xiqiu.north.core;

import top.xiqiu.north.North;
import top.xiqiu.north.util.NorthUtils;

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
     * app 运行环境：local, dev, pre, prod, default ""
     */
    private String env = "";

    /**
     * 初始化配置（单例）
     *
     * <pre>{@code
     * 配置的优先级：args property > 环境变量 > 系统属性 > application-prod.properties > application.properties
     * }</pre>
     */
    public static AppConfig of() {
        if (_appConfig != null) {
            return _appConfig;
        }

        return of(new String[]{});
    }

    /**
     * 初始化配置（单例）
     * @param args 程序启动参数
     */
    public static AppConfig of(String[] args) {
        if (_appConfig == null) {
            _appConfig = new AppConfig();

            // 读取 application.properties 配置文件
            try (InputStream resourceAsStream = _appConfig.getClass().getClassLoader().getResourceAsStream("application.properties")) {
                if (resourceAsStream != null) {
                    _appConfig.load(resourceAsStream);
                }
            } catch (IOException e) {
                // application.properties 不存在
            }

            // 初始化运行环境 AppConfig.env，args property > 环境变量到优先级 > 系统属性
            for (String arg : args) {
                if (arg.length() > 12 && arg.startsWith("--north.env")) {
                    _appConfig.env = arg.substring(12);
                    break;
                }
            }

            if (NorthUtils.isBlank(_appConfig.env)) {
                _appConfig.env = System.getenv("NORTH_ENV");

                if (NorthUtils.isBlank(_appConfig.env)) {
                    _appConfig.env = System.getProperty("north.env", "");
                }
            }

            // System.out.println("north.env=" + _appConfig.env);

            // 支持多套配置文件，
            // 且同时生效 application.properties 相当于基础配置，
            // {env} 对应的配置文件会覆盖 application.properties 相当于配置文件的配置下合并。
            // **使用方法：**
            //   1. java -Dnorth.env=prod -jar xxx.jar
            //   2. NORTH_ENV=prod java -jar xxx.jar
            //   3. java -jar xxx.jar --north.env=prod
            // 多套配置文件的情况优先级：application-prod.properties > application.properties
            if (NorthUtils.isNotBlank(_appConfig.env)) {
                try (InputStream resourceAsStream = _appConfig.getClass().getClassLoader().getResourceAsStream("application-" + _appConfig.env + ".properties")) {
                    if (resourceAsStream != null) {
                        Properties envProperties = new Properties();
                        envProperties.load(resourceAsStream);
                        envProperties.forEach((key, value) -> _appConfig.setProperty(String.valueOf(key), String.valueOf(value)));
                    }
                } catch (IOException e) {
                    // application-{env}.properties 不存在
                }
            }

            // 获取north当前版本号 - 注意：只有在fatjar下生效
            if (North.isAppRunInJar) {
                try (InputStream resourceAsStream = _appConfig.getClass().getClassLoader().getResourceAsStream("META-INF/maven/top.xiqiu/north/pom.properties")) {
                    if (resourceAsStream != null) {
                        Properties northPkgInfo = new Properties();
                        northPkgInfo.load(resourceAsStream);
                        _appConfig.northVersion = northPkgInfo.getProperty("version");
                    }
                } catch (IOException e) {
                    // pom.properties 不存在
                }
            }

            // 加载系统 properties 配置（系统属性优先级高于配置文件）
            System.getProperties().forEach((key, value) -> _appConfig.setProperty(String.valueOf(key), String.valueOf(value)));

            // 加载系统 env 环境变量（环境变量优先级高于系统属性）
            // 优先级：args > 环境变量 > application.properties > Java系统属性
            // 系统内变量将会转为 north.xxx.xx
            System.getenv().forEach((key, value) -> {
                // 把大写的环境变量 NORTH_ABC_DEF 替换成小写的 north.abc.def 覆盖同名的系统属性
                key = key.toLowerCase().replaceAll("_", ".");
                _appConfig.setProperty(key, value);
            });

            // 使用 args 参数覆盖，优先级最高
            for (String arg : args) {
                if (arg.startsWith("--")) {
                    String[] argsItemPart = arg.substring(2).split("=");
                    if (argsItemPart.length == 2) {
                        _appConfig.setProperty(argsItemPart[0], argsItemPart[1]);
                    }
                }
            }
        }

        return _appConfig;
    }

    /**
     * 获取配置 - 字符串键
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
        if (NorthUtils.isEmpty(temp)) {
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

    /**
     * 获取 north 运行环境 env （相当于 north.env 的值）
     */
    public String getEnv() {
        return env;
    }
}
