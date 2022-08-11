package top.xiqiu.north.support;

import java.lang.reflect.Method;

/**
 * DeleteMapping 注解处理
 */
public class DeleteDispatcher extends GetDispatcher {
    public DeleteDispatcher(Object instance, Method method, String[] parameterNames, Class<?>[] parameterClasses) {
        super(instance, method, parameterNames, parameterClasses);
    }
}
