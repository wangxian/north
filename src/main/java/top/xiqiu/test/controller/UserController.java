package top.xiqiu.test.controller;

import top.xiqiu.north.annotation.Controller;
import top.xiqiu.north.annotation.GetMapping;
import top.xiqiu.north.annotation.PostMapping;
import top.xiqiu.north.core.ModelAndView;
import top.xiqiu.test.entity.Login;
import top.xiqiu.test.entity.User;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    private Map<String, User> userDatabase = new HashMap<>() {
        {
            List<User> users = List.of(
                    new User("wx@xiqiu.top", "wx", "溪秋", "I am xiqiu."),
                    new User("tom@example.com", "tom", "汤姆", "This is tom."));

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
            PrintWriter printWriter = response.getWriter();
            printWriter.write("{\"error\":\"bad email or password\"}");
            printWriter.flush();
        } else {
            session.setAttribute("user", user);
            response.setContentType("application/json");
            PrintWriter printWriter = response.getWriter();
            printWriter.write("{\"result\": true}");
            printWriter.flush();
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

