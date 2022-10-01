package top.xiqiu.north.support;

import java.lang.reflect.Method;

/**
 * RequestMapping 注解处理
 */
public class RequestDispatcher extends PostDispatcher {
    public RequestDispatcher(Object instance, Method method, MethodParameter[] methodParameters) {
        super(instance, method, methodParameters);
    }
}
