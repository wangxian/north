package top.xiqiu.north.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Json 获取原始泛型
 * 为了统一 gson/fastjson
 */
public class JsonType<T> {
    public final Type getType() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) superclass;
            return parameterized.getActualTypeArguments()[0];
        }
        // Check for raw TypeToken as superclass
        else if (superclass == JsonType.class) {
            throw new IllegalStateException("TypeToken must be created with a type argument: new JsonType<...>() {}; ");
        }

        // User created subclass of TypeToken
        throw new IllegalStateException("Must only create direct subclasses of JsonType");
    }
}
