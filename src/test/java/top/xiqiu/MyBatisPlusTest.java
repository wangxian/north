package top.xiqiu;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.test.entity.Person;
import top.xiqiu.test.mapper.PersonMapper;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MyBatisPlusTest {
    /**
     * logger
     **/
    private final Logger logger = LoggerFactory.getLogger(MyBatisPlusTest.class);

    @Test
    public void MyBatisPlusTest() {
        final MyBatisPlusTest myBatisPlusTest = new MyBatisPlusTest();
        try (SqlSession session = myBatisPlusTest.initSqlSessionFactory().openSession(true)) {
            PersonMapper personMapper = session.getMapper(PersonMapper.class);

            Person person = new Person();
            person.setName("老李");
            personMapper.insert(person);

            Person p2 = personMapper.selectById(person.getId());

            System.out.println("写入的ID=" + person.getId());
            System.out.println("查询写入名字: " + personMapper.selectById(person.getId()).getName());

            final List<Person> p3 = personMapper.findAll();
            logger.info("p3 = {}, p3.size={}", p3.toString(), p3.size());
        }
    }

    public SqlSessionFactory initSqlSessionFactory() {
        DataSource dataSource = dataSource();
        TransactionFactory transactionFactory = new JdbcTransactionFactory();

        Environment environment = new Environment("production", transactionFactory, dataSource);
        MybatisConfiguration configuration = new MybatisConfiguration(environment);

        configuration.addMapper(PersonMapper.class);
        configuration.setLogImpl(StdOutImpl.class);

        // 开启驼峰大小写转换
        configuration.setMapUnderscoreToCamelCase(true);

        // 配置添加数据自动返回数据主键
        configuration.setUseGeneratedKeys(true);

        //这是初始化连接器，如mybatis-plus的分页插件
        configuration.addInterceptor(initInterceptor());

        // 扫描mapper接口所在包
        // configuration.addMappers("top.xiqiu.test.mapper");

        // 注册扫描的mapper xml
        try {
            this.registryMapperXml(configuration, "mapper");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 构建mybatis-plus需要的globalConfig
        GlobalConfig globalConfig = new GlobalConfig();

        // 此参数会自动生成实现baseMapper的基础方法映射
        globalConfig.setSqlInjector(new DefaultSqlInjector());

        // 设置id生成器
        // globalConfig.setIdentifierGenerator(new DefaultIdentifierGenerator());

        // 设置超类mapper
        globalConfig.setSuperMapperClass(BaseMapper.class);

        // 给configuration注入GlobalConfig里面的配置
        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);

        return new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    /**
     * 解析mapper.xml文件
     *
     * @param configuration
     * @param classPath     classpath:mapper 放置mapper/**.xml
     */
    private void registryMapperXml(MybatisConfiguration configuration, String classPath) throws IOException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> mapper = contextClassLoader.getResources(classPath);

        while (mapper.hasMoreElements()) {
            URL url = mapper.nextElement();
            if (url.getProtocol().equals("file")) {
                String path = url.getPath();
                File file = new File(path);
                File[] files = file.listFiles();

                for (File f : files) {
                    FileInputStream in = new FileInputStream(f);
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(in, configuration, f.getPath(), configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                    in.close();
                }
            } else {
                JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
                JarFile jarFile = urlConnection.getJarFile();
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    if (jarEntry.getName().endsWith(".xml")) {
                        InputStream in = jarFile.getInputStream(jarEntry);
                        XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(in, configuration, jarEntry.getName(), configuration.getSqlFragments());
                        xmlMapperBuilder.parse();
                        in.close();
                    }
                }
            }
        }
    }

    /**
     * 初始化拦截器
     */
    private Interceptor initInterceptor() {
        // 创建mybatis-plus插件对象
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 构建分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        paginationInnerInterceptor.setOverflow(true);
        paginationInnerInterceptor.setMaxLimit(500L);

        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        return interceptor;
    }

    public DataSource dataSource() {
        // SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        HikariDataSource dataSource = new HikariDataSource();

        // spring-jdbc
        // dataSource.setDriverClass(org.h2.Driver.class);
        // dataSource.setUrl("jdbc:h2:mem:test");

        // HikariCP
        // dataSource.setDriverClassName("org.h2.Driver");

        // 支持 p6spy 打印执行时间，注意：jdbcUrl 后面要加上 p6spy
        dataSource.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
        dataSource.setJdbcUrl("jdbc:p6spy:h2:mem:test");

        dataSource.setUsername("root");
        dataSource.setPassword("test");

        dataSource.setIdleTimeout(60000);
        dataSource.setAutoCommit(true);
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(1);
        dataSource.setMaxLifetime(60000 * 10);
        dataSource.setConnectionTestQuery("SELECT 1");

        // 创建数据库
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            // h2 需要使用 1.x 版本，2.x 不支持 int(11) 这种方式
            statement.execute("CREATE TABLE person (" +
                                      "`id` bigint(20) not null AUTO_INCREMENT," +
                                      "`name` varchar(30) null," +
                                      "`age` int(11) null, primary key(`id`))");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 查询数据库状态
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("show tables");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String name = metaData.getColumnLabel(i);
                    String field = resultSet.getString(i);
                    System.out.printf("%s:%s\n", name, field);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return dataSource;
    }
}
