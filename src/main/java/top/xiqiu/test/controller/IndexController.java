package top.xiqiu.test.controller;

import top.xiqiu.north.annotation.Controller;
import top.xiqiu.north.annotation.DeleteMapping;
import top.xiqiu.north.annotation.GetMapping;
import top.xiqiu.north.annotation.PutMapping;
import top.xiqiu.north.core.ModelAndView;
import top.xiqiu.test.entity.Login;
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

    /**
     * 测试 - 删除操作
     */
    @DeleteMapping("/delete")
    public void delete(HttpServletResponse resp, Integer id) throws IOException {
        resp.getWriter().write("delete page id=" + id);
        resp.getWriter().flush();
    }

    /**
     * 测试 - 更新操作
     * curl -X PUT -d '{"email":"abc@def.com"}' http://127.0.0.1:8080/update/1
     */
    @PutMapping("/update/1")
    public void update(HttpServletResponse resp, Login login) throws IOException {
        resp.getWriter().write("update email=" + login.email);
        resp.getWriter().flush();
    }
}
