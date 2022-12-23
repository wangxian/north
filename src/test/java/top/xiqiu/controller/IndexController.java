package top.xiqiu.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.entity.User;
import top.xiqiu.north.annotation.*;
import top.xiqiu.north.support.SecretCookie;
import top.xiqiu.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class IndexController {
    /**
     * logger
     **/
    private final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private User user;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        // 是同一个 User 对象
        // System.out.println(user.hashCode());
        // System.out.println(BeanFactory.getBean(User.class).hashCode());

        logger.info("User user = {}", user.toString());
        userService.sayName();

        return "hello world!";
    }

    @GetMapping("/profile/{id}")
    public String profile(@PathVariable("id") Long id, @RequestParam(value = "name", required = false, defaultValue = "王昊") String name) {
        return String.format("hello world! --- name = %s, id = %d", name, id);
    }

    /**
     * 测试编译参数 -parameters
     * <a href="http://127.0.0.1:8080/test?id=88&name=hello">test link</a>
     */
    @GetMapping("/test")
    public String test(Integer id, String name) {
        return String.format("/test --- id = %d, name = %s", id, name);
    }

    /**
     * 安全 cookie 测试
     * <a href="http://127.0.0.1:8080/secret-cookie-test?id=88&name=hello">test link</a>
     */
    @GetMapping("/secret-cookie-test")
    public String secretCookieTest(HttpServletRequest request, HttpServletResponse response) {
        final SecretCookie secretCookie = new SecretCookie(request, "user-ram-m4", 86400);
        secretCookie.setSecretKey("your-secret-key");

        // 读取cookie
        Map<String, Object> currentCookie = secretCookie.getCookie();
        logger.info("当前安全cookie={}", currentCookie);

        // 以下为写入cookie
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", "wx");
        data.put("id", 133);
        secretCookie.setValue(data);
        secretCookie.setPath("/");

        response.addCookie(secretCookie);

        return "ok";
    }
}
