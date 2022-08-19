package top.xiqiu;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.North;
import top.xiqiu.north.core.JsonConverter;
import top.xiqiu.north.util.NorthUtil;
import top.xiqiu.test.entity.Login;

import java.lang.reflect.Type;
import java.security.Provider;
import java.security.Security;
import java.util.*;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * logger
     **/
    private final Logger logger = LoggerFactory.getLogger(AppTest.class);

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        Assert.assertTrue(true);
    }

    @Test
    public void testJsonConverter2String() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("h", "<body><br /><p id=\"name\"></p></body>");
        data.put("name", "wangxian");
        data.put("now", new Date());

        String jsonString = new JsonConverter().stringify(data);
        System.out.println(jsonString);
    }

    /**
     * gson 方法
     */
    @Test
    public void testJsonConverter2Json1() {
        String jsonString = "{\"now\":\"2022-08-12 17:43:50\",\"h\":\"<body><br /><p id=\\\"name\\\"></p></body>\",\"name\":\"wangxian\"}";
        final Type type = new TypeToken<HashMap<String, Object>>() {
        }.getType();
        final Map<String, Object> parse = new Gson().newBuilder().create().fromJson(jsonString, type);
        System.out.println(parse.get("now"));
    }

    /**
     * JsonConverter 封装 - bean 类
     */
    @Test
    public void testJsonConverter2Json2() {
        String jsonString = "{\"email\":\"wangxian@wboll.com\"}";
        Login login = new JsonConverter().parse(jsonString, Login.class);

        System.out.println("email=" + login.email);
    }

    /**
     * JsonConverter 封装 - 泛型
     */
    @Test
    public void testJsonConverter2Json3() {
        // 解析为 map
        String jsonString = "{\"now\":\"2022-08-12 17:43:50\", \"h\":\"<body><br /><p id=\\\"name\\\"></p></body>\"}";
        final Map<String, Object> userMap = new JsonConverter().<Map<String, Object>>parse(jsonString);
        System.out.println(userMap.get("h"));

        // 解析为 list
        jsonString = "[\"x@x.com\", \"b@b.com\"]";
        List<String> usersList = new JsonConverter().parse(jsonString);
        System.out.println("usersList.get(0)=" + usersList.get(0));

        // 解析为 array
        jsonString = "[{\"email\": \"x@x.com\"}, {\"email\": \"b@b.com\"}]";
        Login[] userArray = new JsonConverter().parse(jsonString, Login[].class);
        System.out.println("userArray[0].email=" + userArray[0].email);

        // // 解析为 List，存在泛型类型擦除丢失的情况，以下在运行时报错
        // jsonString = "[{\"email\": \"x@x.com\"}, {\"email\": \"b@b.com\"}]";
        // List<Login> listLogin = new JsonConverter().<List<Login>>parse(jsonString);
        // String email = listLogin.get(0).email;
        // System.out.println("listLogin.email=" + email);

        // 下面的 Map<String, String> 也被擦除了
        // jsonString = "[{\"email\": \"x@x.com\"}, {\"email\": \"b@b.com\"}]";
        // List<Map<String, String>> listLogin = new JsonConverter().parse(jsonString);
        // String email = listLogin.get(1).get("email");
        // System.out.println("listLogin.email=" + email);

        // 使用 gson 可以执行
        Gson gson = new Gson();
        List<Login> users = gson.fromJson(jsonString, new TypeToken<List<Login>>() {
        }.getType());
        String email2 = users.get(0).email;
        System.out.println("gson.email=" + email2);
    }

    @Test
    public void envAndProperties() {
        // logger.info("system.env={}", System.getenv());
        final Map<String, String> envs = System.getenv();
        for (Map.Entry<String, String> env : envs.entrySet()) {
            logger.info("{} = {}", env.getKey().toLowerCase(), env.getValue());
        }

        System.out.println("...".repeat(20));

        final Properties properties = System.getProperties();
        // 获取方式一
        // properties.forEach((k, v) -> {
        //     logger.info("{} = {}", k, v);
        // });

        // 方式二
        for (Map.Entry<Object, Object> property : properties.entrySet()) {
            logger.info("{} = {}", property.getKey(), property.getValue());
        }
    }

    @Test
    public void appConfig() {
        logger.info("user.dir={}", North.config().get("user.dir", "/tmp"));
        logger.info("server.host={}", North.config().get("server.host", "192.168.1.22"));
        logger.info("server.port={}", North.config().getInt("server.port", 8888));
        logger.info("pwd={}", North.config().get("pwd"));

        Assert.assertEquals(North.config().getInt("server.port", 8888), 8080);
    }

    /**
     * 测试获取 extName
     */
    @Test
    public void extName() {
        System.out.println(NorthUtil.extName("/tmp/abc.txt"));
        System.out.println(NorthUtil.extName("/tmp/abc.png"));
        System.out.println(NorthUtil.extName(" "));
        System.out.println(NorthUtil.extName("abc.txt"));
        System.out.println(NorthUtil.extName("xx.webp"));
        System.out.println(NorthUtil.extName("xx.html"));
        System.out.println(NorthUtil.extName("xx.css"));
        System.out.println(NorthUtil.extName("xx.js"));
        System.out.println(NorthUtil.extName("xx.log"));
    }

    @Test
    public void hashTest() {
        Assert.assertEquals(NorthUtil.md5("111111"), "96e79218965eb72c92a549dd5a330112");
        Assert.assertEquals(NorthUtil.sha1("111111"), "3d4f2bf07dc1be38b20cd6e46949a1071f9d0e3d");

        final Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            // logger.info("provider={}", provider.getInfo());
            provider.forEach((key, value) -> {
                logger.info("{} = {}", key, value);
            });
        }
    }

    private static String testName = "h-1001";

    public Object show() {
        Object obj = new Object();
        try {
            return obj;
        } finally {
            System.out.println("执行finally模块");
            testName = "h-1002";
            obj      = null;
        }
    }

    /**
     * 测试 finally 的返回值 及改变外部常量
     */
    @Test
    public void testFinally() {
        logger.info("show={}, testName= {}", show(), testName);
    }

    @Test
    public void testMap() {
        Map<String, Object> user = Map.of();
        user.put("name", "wx");

        logger.info("user = {}", user);

        // String key = "NORTH_ABC_DEF";
        // key = key.toLowerCase();
        // if (key.startsWith("north")) {
        //     // key = key.replace("_", ".");
        //     key = key.replaceAll("_", ".");
        // }
        //
        // System.out.println(key);
    }

}
