package top.xiqiu.support;

import top.xiqiu.core.ModelAndView;
import top.xiqiu.core.ViewEngine;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * jsp 模版引擎
 */
public class JspViewEngine implements ViewEngine {

    public JspViewEngine(ServletContext servletContext) throws IOException {
    }

    @Override
    public void render(ModelAndView modelAndView, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher(modelAndView.getView()).forward(req, resp);
    }
}
