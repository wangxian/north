package top.xiqiu.north.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.annotation.DeleteMapping;
import top.xiqiu.north.annotation.GetMapping;
import top.xiqiu.north.annotation.PostMapping;
import top.xiqiu.north.annotation.PutMapping;
import top.xiqiu.north.support.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// @WebServlet(urlPatterns = "/")
public class DispatcherServlet extends HttpServlet {
    /**
     * logger
     **/
    private static Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    /**
     * get routes
     */
    private Map<String, GetDispatcher> getMappings = new HashMap<>();

    /**
     * post routes
     */
    private Map<String, PostDispatcher> postMappings = new HashMap<>();

    /**
     * put routes
     */
    private Map<String, PutDispatcher> putMappings = new HashMap<>();

    /**
     * delete routes
     */
    private Map<String, DeleteDispatcher> deleteMappings = new HashMap<>();

    /**
     * template view engine
     */
    private ViewEngine viewEngine;

    /**
     * support get parameter types
     */
    private static final Set<Class<?>> SupportedGetParameterTypes =
            Set.of(Integer.class, Long.class, Boolean.class, String.class, HttpServletRequest.class, HttpServletResponse.class, HttpSession.class);

    /**
     * support post parameter types
     */
    private static final Set<Class<?>> SupportedPostParameterTypes =
            Set.of(HttpServletRequest.class, HttpServletResponse.class, HttpSession.class);

    @Override
    public void init() throws ServletException {
        // super.init();
        logger.info("{} init ...", getClass().getSimpleName());

        // 遍历 controllers，预处理 get/post/put/delete 请求
        for (Class<?> controllerClass : ScanClassWithAnnotations.getStoredControllers()) {
            try {
                Object controllerInstance = controllerClass.getConstructor().newInstance();

                // 处理控制器内的注解方法
                for (Method method : controllerClass.getMethods()) {
                    if (method.getAnnotation(GetMapping.class) != null) {
                        // 检查返回值类型
                        if (method.getReturnType() != ModelAndView.class && method.getReturnType() != void.class) {
                            throw new UnsupportedOperationException("Unsupported return type:" + method.getReturnType() + " for method:" + method);
                        }

                        // 检查形参类型
                        for (Class<?> parameterClass : method.getParameterTypes()) {
                            if (!SupportedGetParameterTypes.contains(parameterClass)) {
                                throw new UnsupportedOperationException("Unsupported parameter type:" + method.getReturnType() + " for method:" + method);
                            }
                        }

                        String[] parameterNames = Arrays.stream(method.getParameterTypes()).map(p -> p.getName()).toArray(String[]::new);
                        String path = method.getAnnotation(GetMapping.class).value();

                        logger.debug("GET route {} => {}", path, method);
                        this.getMappings.put(path, new GetDispatcher(controllerInstance, method, parameterNames, method.getParameterTypes()));
                    } else if (method.getAnnotation(PostMapping.class) != null) {
                        // 检查返回值类型
                        if (method.getReturnType() != ModelAndView.class && method.getReturnType() != void.class) {
                            throw new UnsupportedOperationException("Unsupported return type:" + method.getReturnType() + " for method:" + method);
                        }

                        // 检查形参类型
                        // 注意：不允许多个 entity 类型行参
                        // noinspection DuplicatedCode
                        Class<?> requestBodyClass = null;
                        for (Class<?> parameterClass : method.getParameterTypes()) {
                            if (!SupportedPostParameterTypes.contains(parameterClass)) {
                                if (requestBodyClass == null) {
                                    requestBodyClass = parameterClass;
                                } else {
                                    throw new UnsupportedOperationException("Unsupported duplicate request body type::" + method.getReturnType() + " for method:" + method);
                                }
                            }
                        }

                        String path = method.getAnnotation(PostMapping.class).value();
                        final Gson gson = new GsonBuilder().serializeNulls().create();

                        logger.debug("POST route {} => {}", path, method.getName());
                        this.postMappings.put(path, new PostDispatcher(controllerInstance, method, method.getParameterTypes(), gson));
                    } else if (method.getAnnotation(DeleteMapping.class) != null) {
                        // 检查形参类型
                        // noinspection DuplicatedCode
                        for (Class<?> parameterClass : method.getParameterTypes()) {
                            if (!SupportedGetParameterTypes.contains(parameterClass)) {
                                throw new UnsupportedOperationException("Unsupported parameter type:" + method.getReturnType() + " for method:" + method);
                            }
                        }

                        String[] parameterNames = Arrays.stream(method.getParameterTypes()).map(p -> p.getName()).toArray(String[]::new);
                        String path = method.getAnnotation(DeleteMapping.class).value();

                        logger.debug("DELETE route {} => {}", path, method);
                        this.deleteMappings.put(path, new DeleteDispatcher(controllerInstance, method, parameterNames, method.getParameterTypes()));
                    } else if (method.getAnnotation(PutMapping.class) != null) {
                        // 检查形参类型，不允许多个 entity 类型行参
                        // noinspection DuplicatedCode
                        Class<?> requestBodyClass = null;
                        for (Class<?> parameterClass : method.getParameterTypes()) {
                            if (!SupportedPostParameterTypes.contains(parameterClass)) {
                                if (requestBodyClass == null) {
                                    requestBodyClass = parameterClass;
                                } else {
                                    throw new UnsupportedOperationException("Unsupported duplicate request body type::" + method.getReturnType() + " for method:" + method);
                                }
                            }
                        }

                        String path = method.getAnnotation(PutMapping.class).value();
                        final Gson gson = new GsonBuilder().serializeNulls().create();

                        logger.debug("PUT route {} => {}", path, method.getName());
                        this.putMappings.put(path, new PutDispatcher(controllerInstance, method, method.getParameterTypes(), gson));
                    }
                }
            } catch (ReflectiveOperationException e) {
                throw new ServletException(e);
                // e.printStackTrace();
            }
        }

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
    private void dispatch(HttpServletRequest req, HttpServletResponse resp, Map<String, ? extends RouteDispatcher> dispatcherMap)
            throws IOException, ServletException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getRequestURI().substring(req.getContextPath().length());
        RouteDispatcher routeDispatcher = dispatcherMap.get(path);

        // 路由调度不存在
        if (routeDispatcher == null) {
            resp.sendError(404);
            return;
        }

        // 执行控制器逻辑
        ModelAndView modelAndView = null;
        try {
            modelAndView = routeDispatcher.invoke(req, resp);
        } catch (ReflectiveOperationException e) {
            throw new ServletException(e);
        }

        // 不渲染视图，支持控制器内自己处理响应
        if (modelAndView == null) {
            return;
        }

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
        dispatch(req, resp, this.getMappings);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doPost(req, resp);
        logger.debug("POST {}", req.getRequestURI());
        dispatch(req, resp, this.postMappings);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doPut(req, resp);
        logger.debug("PUT {}", req.getRequestURI());
        dispatch(req, resp, this.putMappings);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doDelete(req, resp);
        logger.debug("DELETE {}", req.getRequestURI());
        dispatch(req, resp, this.deleteMappings);
    }
}
