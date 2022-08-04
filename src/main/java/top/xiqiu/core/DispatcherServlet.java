package top.xiqiu.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// @WebServlet(urlPatterns = "/")
public class DispatcherServlet extends HttpServlet {
    /**
     * logger
     **/
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServlet.class);

    @Override
    public void init() throws ServletException {
        // super.init();

        // LOGGER.info("config={}", getServletConfig());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doGet(req, resp);

        LOGGER.info("request.uri={}", req.getRequestURI());

        if (req.getRequestURI().startsWith("/404")) {
            resp.getWriter().write("404 PAGE NOT FOUND");
            resp.getWriter().flush();
            return;
        } else if (req.getRequestURI().startsWith("/test")) {
            resp.sendError(404);
            return;
        } else if (req.getRequestURI().startsWith("/index")) {
            // 测试，渲染 jsp 视图
            req.setAttribute("name", "north webapp servlet framework");
            req.getRequestDispatcher("/WEB-INF/templates/test.jsp").forward(req, resp);
            return;
        }
        // else if (req.getRequestURI().startsWith("/WEB-INF/templates/")) {
        //     req.getRequestDispatcher("/WEB-INF/templates/test.jsp").forward(req, resp);
        // }

        resp.getWriter().write("a-b-c-d-e");
        resp.getWriter().flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }
}
