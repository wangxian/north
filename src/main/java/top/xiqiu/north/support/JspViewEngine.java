package top.xiqiu.north.support;

import top.xiqiu.north.core.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * jsp 模版引擎
 */
public class JspViewEngine implements ViewEngine {

    public JspViewEngine(ServletContext servletContext) {
    }

    @Override
    public void render(ModelAndView modelAndView, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 默认视图挂载到 WEB-INF 下
        String view = "/WEB-INF/templates/" + modelAndView.getView();

        // 设置模版变量
        modelAndView.getModel().forEach((key, value) -> req.setAttribute(key, value));

        req.getRequestDispatcher(view).forward(req, resp);
    }
}
