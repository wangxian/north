package top.xiqiu.north.support;

import top.xiqiu.north.core.JsonConverter;

import java.lang.reflect.Method;

/**
 * PutMapping 注解处理
 */
public class PutDispatcher extends PostDispatcher {
    public PutDispatcher(Object instance, Method method, String[] parameterNames, Class<?>[] parameterClasses, JsonConverter postParameter) {
        super(instance, method, parameterNames, parameterClasses, postParameter);
    }
}
