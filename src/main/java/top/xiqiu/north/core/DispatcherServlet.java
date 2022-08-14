package top.xiqiu.north.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.North;
import top.xiqiu.north.support.JspViewEngine;
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
        String viewEngine = North.config().get("north.view-engine", "pebble");
        if ("jsp".equals(viewEngine)) {
            this.viewEngine = new PebbleViewEngine(getServletContext());
        } else {
            this.viewEngine = new JspViewEngine(getServletContext());
        }
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

        // 去除 path 中 context 路径的干扰
        String path = req.getRequestURI().substring(req.getContextPath().length());

        // 查找相关的路由处理器
        MethodDispatcher methodDispatcher = dispatcherMap.get(path);

        // 路由调度不存在，响应 404 page
        if (methodDispatcher == null) {
            resp.sendError(404);
            return;
        }

        // 执行控制器逻辑
        Object invokeResult;
        try {
            invokeResult = methodDispatcher.invoke(req, resp);
        } catch (ReflectiveOperationException e) {
            throw new NorthException(e);
        }

        // 如果处理器的结果为null，表示控制器内部处理，不再继续往下执行
        if (invokeResult == null) {
            return;
        }

        // 是否是模版视图
        if (invokeResult instanceof ModelAndView) {
            // 模版视图，设置响应 html 类型内容
            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");

            ModelAndView modelAndView = (ModelAndView) invokeResult;

            // 支持控制器 redirect:/user/index 跳转到其他 URL
            if (modelAndView.getView().startsWith("redirect:")) {
                resp.sendRedirect(modelAndView.getView().substring(9));
                return;
            }

            // 渲染视图
            this.viewEngine.render(modelAndView, req, resp);
        } else {
            // 数据视图，设置响应为 json 类型
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            String jsonResult = new JsonConverter().stringify(invokeResult);
            resp.getWriter().write(jsonResult);
        }

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
