package top.xiqiu.north.support;

import top.xiqiu.north.core.NorthException;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.List;

public class PostConstructProcessor {

    /**
     * 执行 PostConstruct 的方法
     */
    public static void invoke(List<Class<?>> components) {
        for (Class<?> component : components) {
            for (Method method : component.getMethods()) {
                if (method.getAnnotation(PostConstruct.class) != null) {
                    try {
                        Object componentInstance = component.getConstructor().newInstance();
                        method.invoke(componentInstance);
                    } catch (ReflectiveOperationException e) {
                        throw new NorthException(e);
                    }
                }
            }
        }
    }

}
