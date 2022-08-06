package test.controller;

import test.entity.Login;
import test.entity.User;
import top.xiqiu.north.annotation.GetMapping;
import top.xiqiu.north.annotation.PostMapping;
import top.xiqiu.north.core.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserController {

    private Map<String, User> userDatabase = new HashMap<>() {
        {
            List<User> users = List.of(
                    new User("wx@xiqiu.top", "wx", "xiqiu.top", "我是 xiqiu."),
                    new User("tom@example.com", "tom", "Tom", "This is tom."));

            users.forEach(user -> {
                put(user.email, user);
            });
        }
    };

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("login.html");
    }

    @PostMapping("/login")
    public ModelAndView login(Login bean, HttpServletResponse response, HttpSession session) throws IOException {
        User user = userDatabase.get(bean.email);
        if (user == null || !user.password.equals(bean.password)) {
            response.setContentType("application/json");
            PrintWriter pw = response.getWriter();
            pw.write("{\"error\":\"Bad email or password\"}");
            pw.flush();
        } else {
            session.setAttribute("user", user);
            response.setContentType("application/json");
            PrintWriter pw = response.getWriter();
            pw.write("{\"result\":true}");
            pw.flush();
        }

        return null;
    }

    @GetMapping("/logout")
    public ModelAndView logout(HttpSession session) {
        session.removeAttribute("user");
        return new ModelAndView("redirect:/");
    }

    @GetMapping("/user/profile")
    public ModelAndView profile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        return new ModelAndView("profile.html", "user", user);
    }
}

