package top.xiqiu.test;

import top.xiqiu.north.North;
import top.xiqiu.test.controller.IndexController;
import top.xiqiu.test.controller.UserController;

import java.util.List;

public class WebTester {
    public static void main(String[] args) {
        /**
         * all controllers
         */
        List<Class<?>> controllers = List.of(IndexController.class, UserController.class);

        North.start(WebTester.class);
    }
}
