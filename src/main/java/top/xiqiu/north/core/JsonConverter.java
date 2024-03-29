package top.xiqiu.north.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

/**
 * JSON 转换类，方便更换json库
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
                // Configures Gson to apply a specific number strategy during deserialization of Object.
                .setObjectToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
                // Configures Gson to apply a specific number strategy during deserialization of Number.
                .setNumberToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
                // 格式化输出
                // .setPrettyPrinting()
                .create();
    }

    /**
     * Java对象 - 转换为字符串
     *
     * @param obj Java对象
     * @return JSON字符串
     */
    public String stringify(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * 字符串解析为 Java bean
     *
     * @param jsonStr JSON字符串
     * @param jsonType JSON bean 类型
     *
     * @return T
     */
    public <T> T parse(String jsonStr, Class<T> jsonType) {
        return gson.fromJson(jsonStr, jsonType);
    }

    /**
     * 字符串解析为 List/Map（解决泛型擦除的问题）
     *
     * <pre>{@code
     * 用法：
     *  1. ArrayList<User> users = jsonConverter.parse(jsonStr, new JsonType<ArrayList<User>>(){});
     *  2. HashMap<String, String> map2 = jsonConverter.parse(mapStr, new JsonType<HashMap<String, String>>(){})
     * }</pre>
     *
     * @param jsonStr  JSON字符串
     * @param jsonType JSON泛型JsonType包装
     * @return T
     */
    public <T> T parse(String jsonStr, JsonType jsonType) {
        return gson.fromJson(jsonStr, jsonType.getType());
    }
}
