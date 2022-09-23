package top.xiqiu.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.config.User;
import top.xiqiu.north.annotation.Autowired;
import top.xiqiu.north.annotation.Controller;
import top.xiqiu.north.annotation.GetMapping;
import top.xiqiu.north.support.BeanFactory;

@Controller
public class IndexController {
    /**
     * logger
     **/
    private final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private User user;

    @GetMapping("/")
    public String index() {
        // 是同一个 User 对象
        System.out.println(user);
        System.out.println(BeanFactory.getBean(User.class));

        return "hello world!";
    }

}
