package top.xiqiu.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.entity.User;
import top.xiqiu.north.annotation.*;
import top.xiqiu.service.UserService;

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

}
