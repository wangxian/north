package top.xiqiu.north.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * json 转换类，方便更换类库
 */
public class JsonConverter {
    private final Gson gson;

    public JsonConverter() {
        gson = new GsonBuilder()
                // 序列化null
                .serializeNulls()
                // 设置日期时间格式，另有2个重载方法
                // 在序列化和反序化时均生效
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                // 禁此序列化内部类
                .disableInnerClassSerialization()
                // 禁止转义html标签
                .disableHtmlEscaping()
                // 格式化输出
                // .setPrettyPrinting()
                .create();
    }

    /**
     * 转换为字符串
     */
    public String stringify(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * 字符串解析为 Java bean
     */
    public <T> T parse(String jsonStr, Class<T> jsonType) {
        return gson.fromJson(jsonStr, jsonType);
    }

    /**
     * 字符串解析为 Map/List（泛型解析，注意类型擦除）
     */
    public <T> T parse(String jsonStr) {
        // Type type = new TypeToken<T>() {}.getType();

        return gson.fromJson(jsonStr, new TypeToken<T>() {
        }.getType());
    }
}
