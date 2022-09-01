package top.xiqiu;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.db.*;
import top.xiqiu.test.entity.Person;

import javax.sql.DataSource;
import java.sql.SQLException;

public class DbTest {
    /**
     * logger
     **/
    private final Logger logger = LoggerFactory.getLogger(DbTest.class);

    @Test
    public void testDbTemplate() throws SQLException, InterruptedException {
        String path = this.getClass().getClassLoader().getResource(".").getPath();

        // final NorthNonePooledDataSource dataSource = new NorthNonePooledDataSource();

        // 测试 h2
        // dataSource.setDriver("org.h2.Driver");
        // // 显示日志
        // // ;TRACE_LEVEL_SYSTEM_OUT=3
        // dataSource.setUrl("jdbc:h2:file:" + path + "h2-database");
        // dataSource.setUsername("sa");
        // dataSource.setPassword("");

        // // 测试 mysql
        // dataSource.setDriver("com.mysql.cj.jdbc.Driver");
        // dataSource.setUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true");
        // dataSource.setUsername("test");
        // dataSource.setPassword("password");

        DataSource dataSource = DataSourceInitializer.initDataSource();

        logger.info("数据源={}", dataSource.getConnection());

        DbTemplate dbTemplate = new DbTemplate();

        // 设置 DataSource
        // dbTemplate.setDataSource(dataSource);

        // 创建测试表
        // dbTemplate.execute("CREATE TABLE IF NOT EXISTS person (" +
        //                            "`id` int(11) not null AUTO_INCREMENT," +
        //                            "`name` varchar(30) null," +
        //                            "`age` int(11) null, primary key(`id`))");
        //
        // // 插入数据
        // dbTemplate.execute("insert into person (name, age) values ('11-王', 11), ('12-李', 12), ('13-赵', 13), ('12-王', 12), ('13-绿', 13)");

        // // 查询 List<Map> - 无参数
        // List<Map<String, Object>> maps = dbTemplate.queryForList("select * from person");
        // logger.info("查询 无参数 List<Map> = {}", maps);
        //
        // // 删除
        // final int rows1 = dbTemplate.update("delete from person where id = ? and age = ?", new Object[]{1, 113});
        // logger.info("删除的行数 = {}", rows1);
        //
        // // 更新
        // final int rows2 = dbTemplate.update("update person set name=now() where id = ?", new Object[]{5});
        // logger.info("删除的行数 = {}", rows2);
        //
        // 查询 List<Map> - 有参数
        // List<Map<String, Object>> maps2 = dbTemplate.queryForList(
        //         "select * from person where id > ?",
        //         new Object[]{"4"}, new int[]{Types.VARCHAR});
        // logger.info("查询 有参数 List<Map> = {}", maps2);

        // // 查询 List<Map> - 有参数
        // List<Map<String, Object>> maps3 = dbTemplate.queryForList(
        //         "select * from person where name like ?",
        //         new Object[]{"%王%"}, new int[]{Types.VARCHAR});
        // logger.info("查询 有参数 List<Map> = {}", maps3);
        //
        // // 查询 Map - 有参数
        // Map<String, Object> maps4 = dbTemplate.queryForMap(
        //         "select * from person where id = ?",
        //         new Object[]{5});
        // logger.info("查询 Map - 有参数 Map = {}", maps4);

        // // 查询对象
        // Person p1 = dbTemplate.queryForObject(
        //         "select * from person where id = ?", Person.class, 5);
        // logger.info("查询对象 queryForObject = {}", p1);

        // // 查询对象列表
        // List<Person> p2 = dbTemplate.queryForList(
        //         "select * from person where id > ?", Person.class, 2);
        // logger.info("查询列表 - type=Person queryForList = {}", p2);
        //
        // // 查询对象 - 自动添加 LIMIT
        // logger.info("查询对象 自动添加 LIMIT - queryForObject = {}", dbTemplate.queryForObject(
        //         "select * from person where id=2", Person.class));
        // // System.out.println("查询对象 自动添加 LIMIT - queryForObject = " + p3);
        //
        // 测试批量执行
        // dbTemplate.queryForObject("select * from person where id=2", Person.class);
        // dbTemplate.queryForObject("select * from person where id=2", Person.class);
        // dbTemplate.queryForObject("select * from person where id=2", Person.class);
        // dbTemplate.queryForObject("select * from person where id=2", Person.class);
        // dbTemplate.queryForObject("select * from person where id=2", Person.class);
        // dbTemplate.queryForObject("select * from person where id=2", Person.class);
        //
        // // dbTemplate.queryForObject("select * from person where id > ? order by id", Person.class, 1);
        //
        // logger.info("queryForMap auto limit = {}",
        //             dbTemplate.queryForMap("select * from person where id > ? order by id", 1));

        // 插入
        // int count = dbTemplate.update("INSERT INTO person (name, age) VALUES (?, ?)", new Object[]{"xiqiu", 6});
        // System.out.println(count);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO person (name, age) VALUES (?, ?)";
        final SimplePreparedStatementCreator simplePreparedStatementCreator
                = new SimplePreparedStatementCreator(new ArgsTypePreparedStatementSetter(new Object[]{"xiqiu", 6}, null), sql);
        dbTemplate.update(simplePreparedStatementCreator, keyHolder);
        logger.info("插入的数据ID={}", keyHolder.getKey());

    }

    @Test
    public void toUnderlineCase() {
        logger.info("userName = {}", ResultRowToBean.toUnderlineCase("userName"));
        logger.info("userInfoList = {}", ResultRowToBean.toUnderlineCase("userInfoList"));
        logger.info("UserList = {}", ResultRowToBean.toUnderlineCase("UserList"));
        logger.info("user_info = {}", ResultRowToBean.toUnderlineCase("user_info"));
    }

    @Test
    public void testMapper() {

        // final List<Person> list =
        //         DbMapper.of(Person.class)
        //                 .where("id > ?", 0)
        //                 // .leftJoin("user b ON a.id=b.id")
        //                 // .groupBy("id, name")
        //                 .orderBy("id desc")
        //                 .limit(100)
        //                 .findList();
        // System.out.println(list);

        // final Person p1 = DbMapper.of(Person.class)
        //                           .where("id = ?", 2)
        //                           .find();
        // System.out.println("find = " + p1);

        // logger.info("insert id = {}", DbMapper.of(Person.class).field("name, age").insert("xiqiu", 6));

        // 批量插入
        // ArrayList<Object[]> batchArgs = new ArrayList<>();
        // for (int i = 0; i < 50; i++) {
        //     batchArgs.add(new Object[]{"xiqiu-" + i, i});
        // }
        //
        // logger.info("batchInsert 每次影响行数 = {}", DbMapper.of(Person.class).field("name, age").batchInsert(batchArgs));

        // 删除数据
        // logger.info("delete 影响行数 = {}", DbMapper.of(Person.class).where("name = ?", "xiqiu").delete());
        // logger.info("delete 影响行数 = {}", DbMapper.of(Person.class).delete(true));

        // 更新数据
        logger.info("update 影响行数 = {}", DbMapper.of(Person.class)
                                                .field("name")
                                                .where("name = ?", "xiqiu").update("xiqiu-33"));
    }

}
