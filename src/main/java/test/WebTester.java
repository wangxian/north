package test;

import test.controller.IndexController;
import test.controller.UserController;
import top.xiqiu.north.North;

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
