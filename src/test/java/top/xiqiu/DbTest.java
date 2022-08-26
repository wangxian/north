package top.xiqiu;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.db.DbTemplate;
import top.xiqiu.north.db.NorthNonePooledDataSource;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DbTest {
    /**
     * logger
     **/
    private final Logger logger = LoggerFactory.getLogger(DbTest.class);

    @Test
    public void testDbTemplate() throws SQLException {
        final NorthNonePooledDataSource dataSource = new NorthNonePooledDataSource();
        dataSource.setDriver("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:test");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        logger.info("数据源={}", dataSource.getConnection());

        DbTemplate dbTemplate = new DbTemplate();
        // 设置 DataSource
        dbTemplate.setDataSource(dataSource);

        // 创建测试表
        dbTemplate.execute("CREATE TABLE person (" +
                                   "`id` bigint(20) not null AUTO_INCREMENT," +
                                   "`name` varchar(30) null," +
                                   "`age` int(11) null, primary key(`id`))");

        // 插入数据
        dbTemplate.execute("insert into person (name, age) values ('11-王', 11), ('12-李', 12), ('13-赵', 13), ('12-红', 12), ('13-绿', 13)");

        // 查询 List<Map> - 无参数
        List<Map<String, Object>> maps = dbTemplate.queryForList("select * from person");
        logger.info("查询 无参数 List<Map> = {}", maps);

        // 查询 List<Map> - 有参数
        List<Map<String, Object>> maps2 = dbTemplate.queryForList("select * from person where name='13-赵'");
        logger.info("查询 有参数 List<Map> = {}", maps2);
    }
}
