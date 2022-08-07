package top.xiqiu.test.controller;

import top.xiqiu.north.annotation.Controller;
import top.xiqiu.north.annotation.GetMapping;
import top.xiqiu.north.core.ModelAndView;
import top.xiqiu.test.entity.User;

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

    /**
     * 404 global page
     */
    @GetMapping("/404")
    public void page404(HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");

        PrintWriter printWriter = response.getWriter();
        printWriter.write("404 NOT FOUND");
        printWriter.flush();
    }
}