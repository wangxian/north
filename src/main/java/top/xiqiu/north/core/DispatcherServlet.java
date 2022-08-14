package top.xiqiu.north.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.support.MethodDispatcher;
import top.xiqiu.north.support.PebbleViewEngine;
import top.xiqiu.north.support.ViewEngine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

// @WebServlet(urlPatterns = "/")
public class DispatcherServlet extends HttpServlet {
    /**
     * logger
     **/
    private final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    /**
     * template view engine
     */
    private ViewEngine viewEngine;

    @Override
    public void init() {
        // super.init();
        logger.info("{} init ...", getClass().getSimpleName());

        // 初始化模版引擎
        this.viewEngine = new PebbleViewEngine(getServletContext());
    }

    /**
     * 统一调度请求及相应结果
     *
     * @param req           请求
     * @param resp          响应
     * @param dispatcherMap 路由表
     */
    private void dispatch(HttpServletRequest req, HttpServletResponse resp, Map<String, ? extends MethodDispatcher> dispatcherMap)
            throws IOException, ServletException {

        String path = req.getRequestURI().substring(req.getContextPath().length());
        MethodDispatcher methodDispatcher = dispatcherMap.get(path);

        // 路由调度不存在
        if (methodDispatcher == null) {
            resp.sendError(404);
            return;
        }

        // 执行控制器逻辑
        ModelAndView modelAndView;
        try {
            modelAndView = methodDispatcher.invoke(req, resp);
        } catch (ReflectiveOperationException e) {
            throw new ServletException(e);
        }

        // 不渲染视图，支持控制器内自己处理响应
        if (modelAndView == null) {
            return;
        }

        // 设置响应 html 类型内容
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        // 支持控制器 redirect:/user/index 跳转到其他 URL
        if (modelAndView.getView().startsWith("redirect:")) {
            resp.sendRedirect(modelAndView.getView().substring(9));
            return;
        }

        // 渲染视图
        this.viewEngine.render(modelAndView, req, resp);

        // flush thr stream
        resp.getWriter().flush();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doGet(req, resp);
        logger.debug("GET {}", req.getRequestURI());
        dispatch(req, resp, RouteHandler.getGetMappings());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doPost(req, resp);
        logger.debug("POST {}", req.getRequestURI());
        dispatch(req, resp, RouteHandler.getPostMappings());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doPut(req, resp);
        logger.debug("PUT {}", req.getRequestURI());
        dispatch(req, resp, RouteHandler.getPutMappings());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doDelete(req, resp);
        logger.debug("DELETE {}", req.getRequestURI());
        dispatch(req, resp, RouteHandler.getDeleteMappings());
    }
}
