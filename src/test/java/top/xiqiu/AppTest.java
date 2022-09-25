package top.xiqiu;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import top.xiqiu.config.User;
import top.xiqiu.north.North;
import top.xiqiu.north.core.ScanClassWithAnnotations;
import top.xiqiu.north.support.BeanFactory;

import java.util.List;

public class AppTest {
    // Run application
    public static void main(String[] args) {
        North.start(AppTest.class);
    }

    // execute only once, in the starting
    @BeforeClass
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
        final User userBean2 = (User) BeanFactory.getBean("userBean", User.class);

        Assert.assertEquals("测试：：是否是一个对象", userBean, userBean2);
    }
}
