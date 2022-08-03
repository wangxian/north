package top.xiqiu.annotation;

import java.lang.annotation.*;

/**
 * 组件，只能配合 @Configuration 使用，
 * 注解在构造器的方法上
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {
}
