package top.xiqiu.annotation;

import java.lang.annotation.*;

/**
 * 构造完成 + 注入后 触发
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PostConstruct {

}
