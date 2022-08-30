package top.xiqiu.north.db;

import com.zaxxer.hikari.HikariDataSource;
import top.xiqiu.north.core.AppConfig;

import javax.sql.DataSource;

/**
 * 初始化 DataSource 兼容层
 */
public class DataSourceInitializer {
    /**
     * 单例 DataSource
     */
    private static DataSource _datasource;

    public static DataSource initDataSource() {
        if (_datasource != null) {
            return _datasource;
        }

        // 可选配置：hikari | north （默认）
        String dataSourceType = AppConfig.of().get("north.datasource.driver.type", "north");

        if ("hikari".equals(dataSourceType)) {
            HikariDataSource dataSource = new HikariDataSource();

            dataSource.setDriverClassName(AppConfig.of().get("north.datasource.driver", ""));
            dataSource.setJdbcUrl(AppConfig.of().get("north.datasource.url", ""));

            dataSource.setUsername(AppConfig.of().get("north.datasource.username", ""));
            dataSource.setPassword(AppConfig.of().get("north.datasource.password", ""));

            dataSource.setIdleTimeout(AppConfig.of().getLong("north.datasource.hikari.idle-timeout", 60000));
            dataSource.setMinimumIdle(AppConfig.of().getInt("north.datasource.hikari.minimum-idle", 1));
            dataSource.setMaximumPoolSize(AppConfig.of().getInt("north.datasource.hikari.maximum-pool-size", 10));
            dataSource.setMaxLifetime(AppConfig.of().getLong("north.datasource.hikari.max-lifetime", 600000));
            dataSource.setConnectionTimeout(AppConfig.of().getLong("north.datasource.hikari.connection-timeout", 30000));

            // 暂时无需配置和修改
            dataSource.setAutoCommit(true);
            dataSource.setConnectionTestQuery("SELECT 1");

            _datasource = dataSource;
        } else {
            // 默认数据源 north
            NorthNonePooledDataSource dataSource = new NorthNonePooledDataSource();

            dataSource.setDriver(AppConfig.of().get("north.datasource.driver", ""));
            dataSource.setUrl(AppConfig.of().get("north.datasource.url", ""));
            dataSource.setUsername(AppConfig.of().get("north.datasource.username", ""));
            dataSource.setPassword(AppConfig.of().get("north.datasource.password", ""));

            _datasource = dataSource;
        }

        return _datasource;
    }
}
