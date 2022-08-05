package top.xiqiu.north.controller;

import top.xiqiu.north.annotation.Controller;
import top.xiqiu.north.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public void index() {

    }
}
