package top.xiqiu.north.support;

import top.xiqiu.north.core.JsonConverter;

import java.lang.reflect.Method;

/**
 * RequestMapping 注解处理
 */
public class RequestDispatcher extends PostDispatcher {
    public RequestDispatcher(Object instance, Method method, String[] parameterNames, Class<?>[] parameterClasses, JsonConverter postParameter) {
        super(instance, method, parameterNames, parameterClasses, postParameter);
    }
}
