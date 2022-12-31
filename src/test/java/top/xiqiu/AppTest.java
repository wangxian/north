package top.xiqiu;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;
import top.xiqiu.entity.User;
import top.xiqiu.north.North;
import top.xiqiu.north.core.AppConfig;
import top.xiqiu.north.core.JsonConverter;
import top.xiqiu.north.core.JsonType;
import top.xiqiu.north.core.ScanClassWithAnnotations;
import top.xiqiu.north.support.BeanFactory;
import top.xiqiu.north.util.AESUtils;
import top.xiqiu.north.util.DigestUtils;
import top.xiqiu.service.UserService;

import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("unchecked")
public class AppTest {
    // Run application
    public static void main(String[] args) {
        North.start(AppTest.class, args);
    }

    // execute only once, in the starting
    // @BeforeClass
    public static void beforeClass() {
        System.out.println("in before class");
    }

    @Test
    public void testApp() {
        final List<Class<?>> classes = ScanClassWithAnnotations.findClasses(this.getClass().getPackageName());
        // classes.forEach(System.out::println);

        // ScanClassWithAnnotations.scanComponents(classes).forEach(System.out::println);

        // 所有的组件 @Component
        final List<Class<?>> components = ScanClassWithAnnotations.scanComponents(classes);

        // 处理 @PostConstruct 注解
        // PostConstructProcessor.invoke(components);

        // 处理 @Bean 注解
        ScanClassWithAnnotations.scanAndStoreBeans(classes);

        final User userBean = BeanFactory.getBean(User.class);
        // final User userBean = BeanFactory.getBean("userBean", User.class);
        final User userBean2 = BeanFactory.getBean("userBean", User.class);

        Assert.assertEquals("测试：：是否是一个对象", userBean, userBean2);

        ScanClassWithAnnotations.scanAndStoreService(classes);
        UserService userService = BeanFactory.getBean(UserService.class);
        userService.sayName();


    }

    @Test
    public void testStartArgs() {
        // 初始化
        AppConfig.of(new String[]{"--north.env=prod", "--name=north-test"});

        Assert.assertEquals("--north.env 的结果=prod", "prod", AppConfig.of().get("north.env"));
        Assert.assertEquals("north-test", AppConfig.of().get("name"));
        Assert.assertEquals("不存在的值", "", AppConfig.of().get("not-exist", ""));

        // // 扩展测试
        // // NORTH_XXX_COUNT，系统内变量会转为 north.xxx.xx
        // System.out.println("north.xxx.count=" + AppConfig.of().getInt("north.xxx.count", 0));
        //
        // // CUSTOM_VAR
        // System.out.println("CUSTOM_VAR=" + AppConfig.of().get("CUSTOM_VAR", "not-exist"));
    }

    @Test
    public void testJsonConverter() {
        JsonConverter jsonConverter = new JsonConverter();

        final ArrayList<User> users = new ArrayList<>();

        User user1 = new User();
        user1.name = "wx1001";
        users.add(user1);

        User user2 = new User();
        user2.name = "wx1002";
        users.add(user2);

        // 对象转 string
        String jsonStr = jsonConverter.stringify(users);
        System.out.println("jsonStr = " + jsonStr);

        // Gson gson = new GsonBuilder()
        //         // 序列化null
        //         .serializeNulls()
        //         // 设置日期时间格式，另有2个重载方法
        //         // 在序列化和反序化时均生效
        //         .setDateFormat("yyyy-MM-dd HH:mm:ss")
        //         // 禁此序列化内部类
        //         .disableInnerClassSerialization()
        //         // 禁止转义html标签
        //         .disableHtmlEscaping()
        //         // 格式化输出
        //         // .setPrettyPrinting()
        //         .create();

        // List<User> users2 = gson.fromJson(jsonStr, new TypeToken<List<User>>(){}.getType());
        // System.out.println(users2);

        // 注意关键是 {}，否则不能原始泛型
        // List<User> list = new ArrayList<>(){};
        // System.out.println( ((ParameterizedType) list.getClass().getGenericSuperclass()).getActualTypeArguments()[0] );

        // 解析jsonString，封装后的例子
        ArrayList<User> users2 = jsonConverter.parse(jsonStr, new JsonType<ArrayList<User>>() {
        });
        System.out.println(users2);

        // 下面这两种方法是一样的
        // User user3 = jsonConverter.parse(jsonConverter.stringify(user1), User.class);
        User user3 = jsonConverter.parse(jsonConverter.stringify(user1), new JsonType<User>() {
        });
        System.out.println(user3);

        // 测试泛型map的解析
        HashMap<String, String> map = new HashMap<>();
        map.put("name", "wx1005");
        map.put("age", "35");

        String mapStr = jsonConverter.stringify(map);
        System.out.println(mapStr);

        // 下面两种方法都可以
        // HashMap<String, String> map2 = jsonConverter.parse(mapStr, HashMap.class);
        HashMap<String, String> map2 = jsonConverter.parse(mapStr, new JsonType<HashMap<String, String>>() {
        });
        System.out.println(map2);
    }


    /**
     * hash 测试
     */
    @Test
    public void testDigest() {
        System.out.println("DigestUtils.getMD5(\"111111\") = " + DigestUtils.getMD5("111111"));
        System.out.println("DigestUtils.getSHA1(\"111111\") = " + DigestUtils.getSHA1("111111"));
        System.out.println("DigestUtils.getSHA256(\"111111\") = " + DigestUtils.getSHA256("111111"));
        System.out.println("DigestUtils.getHmacSHA1(\"111111\", \"sign-key\") = " + DigestUtils.getHmacSHA1("111111", "sign-key"));
    }

    @Test
    public void testAESUtil() {
        // 加密
        String key = "ab033b13cb4ac300";
        String encryptText = AESUtils.encrypt(key, "name=wx-------time=" + new Date().getTime());
        System.out.println("encryptText = " + encryptText);

        // 解密
        String plainText = AESUtils.decrypt(key, encryptText);
        System.out.printf("plainText = " + plainText);
    }

    @Test
    public void testGsonInt2Double() {
        Gson gson = new Gson();
        String json = "{\"key1\":11,\"key2\":2.0,\"key3\":\"3\"}";

        Map m = gson.fromJson(json, Map.class);

        // output 11.0
        System.out.println(m.get("key1"));
        // output 2.0
        System.out.println(m.get("key2"));
        // output 3 <string>
        System.out.println(m.get("key3"));

        final JsonConverter jsonConverter = new JsonConverter();
        Map jsonData = jsonConverter.parse(json, Map.class);
        // output {key1=11, key2=2.0, key3=3}
        System.out.println(jsonData);

        Map jsonData2 = new HashMap();
        jsonData2.put("key1", 11);
        jsonData2.put("key2", 2.0);
        jsonData2.put("key3", "3");
        jsonData2.put("key4", Integer.valueOf("2"));
        jsonData2.put("key5", Float.valueOf("1.01234567890123456789"));
        jsonData2.put("key5-2", Double.valueOf("1.01234567890123456789"));
        jsonData2.put("key6", new BigDecimal("3.01234567890123456789"));
        // output {"key1":11,"key2":2.0,"key3":"3","key4":2,"key5":1.0123457,"key5-2":1.0123456789012346,"key6":3.01234567890123456789}
        System.out.println(jsonConverter.stringify(jsonData2));

    }
}
