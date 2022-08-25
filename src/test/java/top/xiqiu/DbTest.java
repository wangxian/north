package top.xiqiu;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.db.DbTemplate;
import top.xiqiu.north.db.NorthNonePooledDataSource;

import java.sql.JDBCType;
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
        dbTemplate.setDataSource(dataSource);

        dbTemplate.execute("CREATE TABLE person (" +
                                   "`id` bigint(20) not null AUTO_INCREMENT," +
                                   "`name` varchar(30) null," +
                                   "`age` int(11) null, primary key(`id`))");

        dbTemplate.execute("insert into person (name, age) values ('11', 11), ('12', 12), ('13', 13), ('12', 12), ('13', 13)");

        final List<Map<String, Object>> maps = dbTemplate.queryForList("select * from person where id > ?", new Object[]{0}, new int[]{JDBCType.INTEGER.getVendorTypeNumber()});
        logger.info("新插入的数据={}", maps);
    }
}
