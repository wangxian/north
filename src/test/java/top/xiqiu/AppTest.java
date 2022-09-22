package top.xiqiu;

import org.junit.Test;
import top.xiqiu.north.North;
import top.xiqiu.north.core.ScanClassWithAnnotations;
import top.xiqiu.north.support.PostConstructHandler;

import java.util.List;

public class AppTest {
    // Run application
    public static void main(String[] args) {
        North.start(AppTest.class);
    }

    @Test
    public void testApp() {
        final List<Class<?>> classes = ScanClassWithAnnotations.findClasses(this.getClass().getPackageName());
        // classes.forEach(System.out::println);

        // ScanClassWithAnnotations.scanComponents(classes).forEach(System.out::println);

        // 所有的组件 @Component
        final List<Class<?>> components = ScanClassWithAnnotations.scanComponents(classes);

        // 处理 @PostConstruct 注解
        PostConstructHandler.invoke(components);
    }
}
