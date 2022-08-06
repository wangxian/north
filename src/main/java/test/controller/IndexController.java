package test.controller;

import test.entity.User;
import top.xiqiu.north.annotation.Controller;
import top.xiqiu.north.annotation.GetMapping;
import top.xiqiu.north.core.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class IndexController {

    @GetMapping("/")
    public ModelAndView index(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return new ModelAndView("index.html", "user", user);
    }

    @GetMapping("/hello")
    public ModelAndView hello(String name) {
        if (name == null) {
            name = "World";
        }

        return new ModelAndView("hello.html", "name", name);
    }

    @GetMapping("/404")
    public void page404(HttpServletResponse response) throws IOException {
        PrintWriter printWriter = response.getWriter();
        printWriter.write("404 NOT FOUND");
        printWriter.flush();
    }
}
