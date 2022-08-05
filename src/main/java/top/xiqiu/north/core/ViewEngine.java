package top.xiqiu.north.core;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 模版引擎接口
 */
public interface ViewEngine {

    void render(ModelAndView modelAndView, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException;

}
