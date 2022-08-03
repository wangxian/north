package top.xiqiu.annotation;

import java.lang.annotation.*;

/**
 * 托管组件
 * 注解在类上
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    String value() default "";
}
