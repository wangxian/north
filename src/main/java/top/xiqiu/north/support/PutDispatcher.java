package top.xiqiu.north.support;

import com.google.gson.Gson;

import java.lang.reflect.Method;

/**
 * PutMapping 注解处理
 */
public class PutDispatcher extends PostDispatcher {
    public PutDispatcher(Object instance, Method method, Class<?>[] parameterClasses, Gson postParameter) {
        super(instance, method, parameterClasses, postParameter);
    }
}
