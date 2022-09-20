package top.xiqiu;

import org.junit.Test;
import top.xiqiu.north.core.ScanClassWithAnnotations;

import java.util.List;

public class AppTest {
    @Test
    public void testApp() {
        final List<Class<?>> classes = ScanClassWithAnnotations.findClasses(this.getClass().getPackageName());
        // classes.forEach(System.out::println);

        ScanClassWithAnnotations.scanComponents(classes).forEach(System.out::println);
    }
}
