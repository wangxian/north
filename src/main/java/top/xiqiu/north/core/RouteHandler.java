package top.xiqiu.north.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.annotation.*;
import top.xiqiu.north.support.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RouteHandler {
    /**
     * logger
     **/
    private final static Logger logger = LoggerFactory.getLogger(RouteHandler.class);

    /**
     * get routes
     */
    private final static Map<String, GetDispatcher> getMappings = new HashMap<>();

    /**
     * post routes
     */
    private final static Map<String, PostDispatcher> postMappings = new HashMap<>();

    /**
     * request routes
     */
    private final static Map<String, PostDispatcher> requestMappings = new HashMap<>();

    /**
     * put routes
     */
    private final static Map<String, PutDispatcher> putMappings = new HashMap<>();

    /**
     * delete routes
     */
    private final static Map<String, DeleteDispatcher> deleteMappings = new HashMap<>();

    /**
     * support method parameter types
     */
    private static final Set<Class<?>> SUPPORT_PARAMETER_TYPES =
            Set.of(Integer.class, Long.class, Float.class, Double.class, Boolean.class, String.class,
                   HttpServletRequest.class, HttpServletResponse.class, HttpSession.class);


    /**
     * 预处理控制器中的 xxxMapping 注解
     */
    public static void processMappings() {
        // 遍历 controllers 预处理 get/post/put/delete 注解
        for (Class<?> controllerClass : ScanClassWithAnnotations.getStoredControllers()) {
            try {
                Object controllerInstance = controllerClass.getConstructor().newInstance();

                // 控制器默认URI
                String controllerContextPath = controllerClass.getAnnotation(Controller.class).value();
                // 去除控制器路径结尾的 '/'
                if (controllerContextPath.endsWith("/")) {
                    controllerContextPath = controllerContextPath.substring(0, controllerContextPath.length() - 1);
                }

                // 控制器支持 @Autowired
                Arrays.stream(controllerClass.getDeclaredFields()).filter(field -> field.getAnnotation(Autowired.class) != null).forEach(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(controllerInstance, BeanFactory.getBean(field.getType()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });

                // 处理控制器内的注解方法
                for (Method method : controllerClass.getMethods()) {

                    // 参数名称
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Parameter[] parameters = method.getParameters();

                    // 整理形参结构
                    MethodParameter[] methodParameters = new MethodParameter[parameters.length];

                    for (int i = 0; i < parameters.length; i++) {
                        Parameter p = parameters[i];

                        MethodParameter methodParameter = new MethodParameter();
                        methodParameter.setName(p.getName());
                        methodParameter.setClassType(parameterTypes[i]);

                        if (p.getAnnotation(RequestParam.class) != null) {
                            RequestParam requestParam = p.getAnnotation(RequestParam.class);
                            if (requestParam.value() != null && !"".equals(requestParam.value())) {
                                methodParameter.setName(requestParam.value());
                            }

                            methodParameter.setDefaultValue(requestParam.defaultValue());
                            methodParameter.setRequired(requestParam.required());
                        }

                        if (p.getAnnotation(PathVariable.class) != null) {
                            String name = p.getAnnotation(PathVariable.class).value();
                            if (name != null && !"".equals(name)) {
                                methodParameter.setName(name);
                            }
                        }

                        methodParameters[i] = methodParameter;
                    }

                    if (method.getAnnotation(GetMapping.class) != null || method.getAnnotation(DeleteMapping.class) != null) {
                        // // 检查返回值类型
                        // if (method.getReturnType() != ModelAndView.class && method.getReturnType() != void.class) {
                        //     throw new UnsupportedOperationException("Unsupported return type:" + method.getReturnType() + " for method:" + method);
                        // }

                        // 检查形参类型
                        // noinspection DuplicatedCode
                        for (Class<?> parameterClass : method.getParameterTypes()) {
                            if (!SUPPORT_PARAMETER_TYPES.contains(parameterClass)) {
                                throw new UnsupportedOperationException("Unsupported parameter type:" + method.getReturnType() + " for method:" + method);
                            }
                        }

                        if (method.getAnnotation(GetMapping.class) != null) {
                            String path = controllerContextPath + method.getAnnotation(GetMapping.class).value();

                            logger.info("[north] [route]     GET {} -> {}", path, getReadableMethodName(method));
                            getMappings.put(path, new GetDispatcher(controllerInstance, method, methodParameters));
                        } else {
                            String path = controllerContextPath + method.getAnnotation(DeleteMapping.class).value();

                            logger.info("[north] [route]  DELETE {} -> {}", path, getReadableMethodName(method));
                            deleteMappings.put(path, new DeleteDispatcher(controllerInstance, method, methodParameters));
                        }
                    } else if (method.getAnnotation(PostMapping.class) != null
                            || method.getAnnotation(RequestMapping.class) != null
                            || method.getAnnotation(PutMapping.class) != null) {

                        // // 检查返回值类型
                        // if (method.getReturnType() != ModelAndView.class && method.getReturnType() != void.class) {
                        //     throw new UnsupportedOperationException("Unsupported return type:" + method.getReturnType() + " for method:" + method);
                        // }

                        // 检查形参类型（不允许多个 entity 类型行参，否则不能识别那个是那个啦）
                        // noinspection DuplicatedCode
                        Class<?> requestBodyClass = null;
                        for (Class<?> parameterClass : method.getParameterTypes()) {
                            if (!SUPPORT_PARAMETER_TYPES.contains(parameterClass)) {
                                if (requestBodyClass == null) {
                                    requestBodyClass = parameterClass;
                                } else {
                                    throw new UnsupportedOperationException("Unsupported duplicate entity parameter type:" + requestBodyClass.getSimpleName() + " for method:" + method);
                                }
                            }
                        }

                        if (method.getAnnotation(PostMapping.class) != null) {
                            String path = controllerContextPath + method.getAnnotation(PostMapping.class).value();

                            logger.info("[north] [route]    POST {} -> {}", path, getReadableMethodName(method));
                            postMappings.put(path, new PostDispatcher(controllerInstance, method, methodParameters));
                        } else if (method.getAnnotation(RequestMapping.class) != null) {
                            String path = controllerContextPath + method.getAnnotation(RequestMapping.class).value();

                            logger.info("[north] [route] REQUEST {} -> {}", path, getReadableMethodName(method));
                            requestMappings.put(path, new RequestDispatcher(controllerInstance, method, methodParameters));
                        } else {
                            String path = controllerContextPath + method.getAnnotation(PutMapping.class).value();

                            logger.info("[north] [route]     PUT {} -> {}", path, getReadableMethodName(method));
                            putMappings.put(path, new PutDispatcher(controllerInstance, method, methodParameters));
                        }
                    }
                }
            } catch (ReflectiveOperationException e) {
                throw new NorthException(e);
            }
        }
    }

    /**
     * 获取要打印的控制器和方法名称，方便打印
     */
    private static String getReadableMethodName(Method method) {
        String str = method.toString();
        str = str.substring(0, str.lastIndexOf("("));

        String methodName = str.substring(str.lastIndexOf(".") + 1);
        str = str.substring(0, str.lastIndexOf("."));
        String controllerName = str.substring(str.lastIndexOf(".") + 1);

        return controllerName + "#" + methodName;
    }

    /**
     * 查找相关的路由处理器
     */
    public static MethodDispatcher findDispatcher(HttpServletRequest req) {
        // 去除 path 中 context 路径的干扰
        String path = req.getRequestURI().substring(req.getContextPath().length());

        MethodDispatcher dispatcher = null;
        switch (req.getMethod()) {
            case "GET":
                dispatcher = getMappings.get(path);
                break;
            case "POST":
                dispatcher = postMappings.get(path);
                break;
            case "PUT":
                dispatcher = putMappings.get(path);
                break;
            case "DELETE":
                dispatcher = deleteMappings.get(path);
                break;
        }

        // @RequestMapping 如果和普通 get/post/put/delete 重复，那么优先走普通 mapping
        if (dispatcher != null) {
            return dispatcher;
        }

        // 返回 requestMappings 的处理器，但也可能路径还是不存在，最后还是返回 null，匹配失败
        MethodDispatcher requestDispatcher = requestMappings.get(path);
        if (requestDispatcher != null) {
            return requestDispatcher;
        }

        // 如果还是不存在，开始匹配 PathVariable
        //----------------------------------

        // 查找可用的 dispatcher
        HashMap<String, MethodDispatcher> methodDispatchers = new HashMap<>();
        switch (req.getMethod()) {
            case "GET":
                getMappings.forEach((key, value) -> methodDispatchers.merge(key, value, (v1, v2) -> v1));
                break;
            case "POST":
                postMappings.forEach((key, value) -> methodDispatchers.merge(key, value, (v1, v2) -> v1));
                break;
            case "PUT":
                putMappings.forEach((key, value) -> methodDispatchers.merge(key, value, (v1, v2) -> v1));
                break;
            case "DELETE":
                deleteMappings.forEach((key, value) -> methodDispatchers.merge(key, value, (v1, v2) -> v1));
                break;
        }

        // 合并 request 路由
        requestMappings.forEach((key, value) -> methodDispatchers.merge(key, value, (v1, v2) -> v1));

        // 兼容尾部是 / 的URL
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String[] requestUrlPart = path.substring(1).split("/");

        for(Map.Entry<String, MethodDispatcher> entry : methodDispatchers.entrySet()) {
            String routePath = entry.getKey();

            if (routePath.length() > 1 && routePath.endsWith("/")) {
                routePath = routePath.substring(0, routePath.length() - 1);
            }

            String[] routeUrlPart = routePath.substring(1).split("/");

            // url 长度不一样，直接跳过
            if (requestUrlPart.length != routeUrlPart.length) {
                continue;
            }

            HashMap<String, String> pathVariable = new HashMap<>();
            for (int i = 0; i < routeUrlPart.length; i++) {
                if (routeUrlPart[i].contains("{")) {
                    pathVariable.put(routeUrlPart[i].substring(1, routeUrlPart[i].length() - 1), requestUrlPart[i]);
                } else if (!routeUrlPart[i].equals(requestUrlPart[i])) {
                    // 匹配失败，重置
                    pathVariable = new HashMap<String, String>();
                    break;
                }
            }

            // 匹配成功
            if (pathVariable.size() > 0) {
                pathVariable.forEach((key, value) -> req.setAttribute("_path_variable_" + key, value));
                return entry.getValue();
            }
        }

        return null;
    }
}
