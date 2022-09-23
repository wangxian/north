package top.xiqiu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.annotation.Component;

import javax.annotation.PostConstruct;

@Component
public class TestConfig {
    /**
     * logger
     **/
    private final Logger logger = LoggerFactory.getLogger(TestConfig.class);

    @PostConstruct
    public void testAbc() {
        System.out.println("---------- 这里是 @Component TestConfig.testAbc");
    }

    @PostConstruct
    public void testDef() {
        System.out.println("---------- 这里是 @Component TestConfig.testDef");
    }
}
