package top.xiqiu;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;
import top.xiqiu.north.core.JsonConverter;
import top.xiqiu.test.entity.Login;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        Assert.assertTrue(true);
    }

    @Test
    public void testJsonConverter2String() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("h", "<body><br /><p id=\"name\"></p></body>");
        data.put("name", "wangxian");
        data.put("now", new Date());

        String jsonString = new JsonConverter().stringify(data);
        System.out.println(jsonString);
    }

    /**
     * gson 方法
     */
    @Test
    public void testJsonConverter2Json1() {
        String jsonString = "{\"now\":\"2022-08-12 17:43:50\",\"h\":\"<body><br /><p id=\\\"name\\\"></p></body>\",\"name\":\"wangxian\"}";
        final Type type = new TypeToken<HashMap<String, Object>>() {
        }.getType();
        final Map<String, Object> parse = new Gson().newBuilder().create().fromJson(jsonString, type);
        System.out.println(parse.get("now"));
    }

    /**
     * JsonConverter 封装 - bean 类
     */
    @Test
    public void testJsonConverter2Json2() {
        String jsonString = "{\"email\":\"wangxian@wboll.com\"}";
        Login login = new JsonConverter().parse(jsonString, Login.class);

        System.out.println("email=" + login.email);
    }

    /**
     * JsonConverter 封装 - 泛型
     */
    @Test
    public void testJsonConverter2Json3() {
        // 解析为 map
        String jsonString = "{\"now\":\"2022-08-12 17:43:50\", \"h\":\"<body><br /><p id=\\\"name\\\"></p></body>\"}";
        final Map<String, Object> userMap = new JsonConverter().<Map<String, Object>>parse(jsonString);
        System.out.println(userMap.get("h"));

        // 解析为 list
        jsonString = "[\"x@x.com\", \"b@b.com\"]";
        List<String> usersList = new JsonConverter().parse(jsonString);
        System.out.println("usersList.get(0)=" + usersList.get(0));

        // 解析为 array
        jsonString = "[{\"email\": \"x@x.com\"}, {\"email\": \"b@b.com\"}]";
        Login[] userArray = new JsonConverter().parse(jsonString, Login[].class);
        System.out.println("userArray[0].email=" + userArray[0].email);

        // // 解析为 List，存在泛型类型擦除丢失的情况，以下在运行时报错
        // jsonString = "[{\"email\": \"x@x.com\"}, {\"email\": \"b@b.com\"}]";
        // List<Login> listLogin = new JsonConverter().<List<Login>>parse(jsonString);
        // String email = listLogin.get(0).email;
        // System.out.println("listLogin.email=" + email);

        // 下面的 Map<String, String> 也被擦除了
        // jsonString = "[{\"email\": \"x@x.com\"}, {\"email\": \"b@b.com\"}]";
        // List<Map<String, String>> listLogin = new JsonConverter().parse(jsonString);
        // String email = listLogin.get(1).get("email");
        // System.out.println("listLogin.email=" + email);

        // 使用 gson 可以执行
        Gson gson = new Gson();
        List<Login> users = gson.fromJson(jsonString, new TypeToken<List<Login>>() {
        }.getType());
        String email2 = users.get(0).email;
        System.out.println("gson.email=" + email2);
    }
}
