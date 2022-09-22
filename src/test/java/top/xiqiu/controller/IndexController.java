package top.xiqiu.controller;

import top.xiqiu.north.annotation.Controller;
import top.xiqiu.north.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "hello world!";
    }

}
