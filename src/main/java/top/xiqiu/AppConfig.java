package top.xiqiu;

public class AppConfig {
    private static AppConfig _appConfig;

    public static AppConfig getInstance() {
        if (_appConfig == null) {
            _appConfig = new AppConfig();
        }

        return _appConfig;
    }

    /**
     * 初始化 App 配置
     */
    public void initConfig() {

    }

    public int getInt(String prop) {
        return Integer.getInteger(prop);
    }

    public int getIntOrDefault(String prop, int defaultValue) {
        return Integer.getInteger(prop, defaultValue);
    }
}
