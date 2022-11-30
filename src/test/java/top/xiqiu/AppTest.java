package top.xiqiu;

import org.junit.Assert;
import org.junit.Test;
import top.xiqiu.entity.User;
import top.xiqiu.north.North;
import top.xiqiu.north.core.AppConfig;
import top.xiqiu.north.core.JsonConverter;
import top.xiqiu.north.core.JsonType;
import top.xiqiu.north.core.ScanClassWithAnnotations;
import top.xiqiu.north.support.BeanFactory;
import top.xiqiu.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
}
