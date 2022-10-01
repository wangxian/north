package top.xiqiu.north.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.North;
import top.xiqiu.north.support.*;
import top.xiqiu.north.util.NorthUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

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

        // 初始化模版引擎，如果在配置中设置 north.view-engine = no 则相当于禁用模版引擎
        String viewEngine = North.config().get("north.view-engine", "no");
        if ("pebble".equals(viewEngine)) {
            this.viewEngine = new PebbleViewEngine(getServletContext());
        } else if ("jsp".equals(viewEngine)) {
            this.viewEngine = new JspViewEngine(getServletContext());
        }
    }

    /**
     * 统一调度请求及相应结果
     *
     * @param req           请求
     * @param resp          响应
     */
    private void dispatch(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // 拦截网络请求
        List<URLInterceptorAdapter> interceptors = ScanClassWithAnnotations.getStoredInterceptors();
        for (URLInterceptorAdapter interceptor : interceptors) {
            try {
                // 被拦截，不再继续往下执行
                if (!interceptor.preHandle(req, resp)) {
                    return;
                }
            } catch (Exception e) {
                throw new NorthException(e);
            }
        }

        // 查找相关的路由处理器
        MethodDispatcher methodDispatcher = RouteHandler.findDispatcher(req);

        // 路由调度不存在，响应 404 page
        if (methodDispatcher == null) {
            resp.sendError(404);
            return;
        }

        // 执行控制器逻辑
        Object invokeResult;
        try {
            invokeResult = methodDispatcher.invoke(req, resp);
        } catch (InvocationTargetException e) {
            // e.getTargetException().printStackTrace();
            logger.error("InvocationTargetException = ", e.getTargetException());

            String errorPage500 = North.config().get("north.error-page-500", "");
            if (!"".equals(errorPage500)) {
                req.setAttribute("errorMessage", e.getTargetException().getMessage());
                req.setAttribute("errorStackTrace", getStackTraceString(e.getTargetException()));
                req.getRequestDispatcher(errorPage500).forward(req, resp);
                return;
            } else {
                invokeResult = getStackTraceString(e.getTargetException());
            }
        } catch (ReflectiveOperationException e) {
            logger.error("ReflectiveOperationException = ", e);
            invokeResult = getStackTraceString(e);
            // return;
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

            // 如果模版引擎被禁用，而在控制器中选择渲染视图，则抛出错误
            if (this.viewEngine == null) {
                throw new NorthException("模版引擎未被正确初始化（可能在配置中被禁用），渲染模版失败～");
            }

            // 渲染视图
            this.viewEngine.render(modelAndView, req, resp);
        } else if (invokeResult instanceof String) {
            // 响应为文本字符串
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write(invokeResult.toString());
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

    /**
     * 获取异常堆栈信息
     */
    private String getStackTraceString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        e.printStackTrace(printWriter);
        printWriter.close();

        return stringWriter.toString();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doGet(req, resp);
        logger.debug("GET {}", req.getRequestURI() + (NorthUtil.isNotBlank(req.getQueryString()) ? "?" + req.getQueryString() : ""));
        dispatch(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doPost(req, resp);
        logger.debug("POST {}", req.getRequestURI() + (NorthUtil.isNotBlank(req.getQueryString()) ? "?" + req.getQueryString() : ""));
        dispatch(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doPut(req, resp);
        logger.debug("PUT {}", req.getRequestURI() + (NorthUtil.isNotBlank(req.getQueryString()) ? "?" + req.getQueryString() : ""));
        dispatch(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doDelete(req, resp);
        logger.debug("DELETE {}", req.getRequestURI() + (NorthUtil.isNotBlank(req.getQueryString()) ? "?" + req.getQueryString() : ""));
        dispatch(req, resp);
    }
}
