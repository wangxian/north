package top.xiqiu.north.annotation;

import java.lang.annotation.*;

/**
 * 配置器，动态构建组件 或 适配些接口
 * 注解在类上
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configuration {
}
