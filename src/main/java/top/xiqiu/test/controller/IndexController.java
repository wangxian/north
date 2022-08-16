package top.xiqiu.test.controller;

import top.xiqiu.north.annotation.*;
import top.xiqiu.north.core.ModelAndView;
import top.xiqiu.test.entity.Login;
import top.xiqiu.test.entity.User;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

@Controller("/")
public class IndexController {

    @GetMapping("/")
    public ModelAndView index(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return new ModelAndView("index.html", "user", user);

        // // test jsp view engine
        // String user = "guest";
        // return new ModelAndView("test.jsp", "user", user);
    }

    /**
     * 测试 @RequestMapping
     * curl -X GET http://127.0.0.1:8080/request/mapping?name=guest
     * curl -X POST http://127.0.0.1:8080/request/mapping?name=guest
     */
    @RequestMapping("/request/mapping")
    public String requestMapping(String name) {
        return "user:" + name + "\nnow=" + new Date();
    }

    @GetMapping("/hello")
    public ModelAndView hello(String name) {
        if (name == null) {
            name = "World";
        }

        return new ModelAndView("hello.html", "name", name);
    }

    /**
     * http://127.0.0.1:8080/bean/json
     */
    @GetMapping("/bean/json")
    public User getUser() {
        User user = new User();
        user.email    = "a@company.com";
        user.password = "b**a";

        return user;
    }

    /**
     * http://127.0.0.1:8080/map/json
     */
    @GetMapping("/map/json")
    public HashMap<String, Object> getMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("email", "a@company.com");
        map.put("name", "xiao.er");
        map.put("birthday", new Date());

        return map;
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
     * curl -X DELETE http://127.0.0.1:8080/delete?id=1
     */
    @DeleteMapping("/delete")
    public void delete(HttpServletResponse resp, Long id) throws IOException {
        resp.getWriter().write("delete page id=" + id);
        resp.getWriter().flush();
    }

    /**
     * 测试 - 更新操作
     * curl -X PUT -d '{"email":"abc@def.com"}' http://127.0.0.1:8080/update?id=98
     */
    @PutMapping("/update")
    public void update(HttpServletResponse resp, Integer id, Login login) throws IOException {
        resp.getWriter().write("update id=" + id + ", email=" + login.email);
        resp.getWriter().flush();
    }
}
