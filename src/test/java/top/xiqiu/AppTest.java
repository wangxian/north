package top.xiqiu;

import org.junit.Assert;
import org.junit.Test;
import top.xiqiu.entity.User;
import top.xiqiu.north.North;
import top.xiqiu.north.core.AppConfig;
import top.xiqiu.north.core.ScanClassWithAnnotations;
import top.xiqiu.north.support.BeanFactory;
import top.xiqiu.service.UserService;

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
}
