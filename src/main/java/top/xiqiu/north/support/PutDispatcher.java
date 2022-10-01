package top.xiqiu.north.support;

import java.lang.reflect.Method;

/**
 * PutMapping 注解处理
 */
public class PutDispatcher extends PostDispatcher {
    public PutDispatcher(Object instance, Method method, MethodParameter[] methodParameters) {
        super(instance, method, methodParameters);
    }
}
