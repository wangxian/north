package top.xiqiu.north.annotation;

import java.lang.annotation.*;

/**
 * 注入Bean
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    String value() default "";
}
