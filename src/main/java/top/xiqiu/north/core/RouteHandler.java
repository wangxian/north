package top.xiqiu.north.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.annotation.*;
import top.xiqiu.north.support.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
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

                // 处理控制器内的注解方法
                for (Method method : controllerClass.getMethods()) {
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

                        // 参数名称
                        String[] parameterNames = Arrays.stream(method.getParameters()).map(p -> p.getName()).toArray(String[]::new);

                        if (method.getAnnotation(GetMapping.class) != null) {
                            String path = controllerContextPath + method.getAnnotation(GetMapping.class).value();

                            logger.info("[north] [route]     GET {} -> {}", path, getReadableMethodName(method));
                            getMappings.put(path, new GetDispatcher(controllerInstance, method, parameterNames, method.getParameterTypes()));
                        } else {
                            String path = controllerContextPath + method.getAnnotation(DeleteMapping.class).value();

                            logger.info("[north] [route]  DELETE {} -> {}", path, getReadableMethodName(method));
                            deleteMappings.put(path, new DeleteDispatcher(controllerInstance, method, parameterNames, method.getParameterTypes()));
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

                        // 参数名称
                        String[] parameterNames = Arrays.stream(method.getParameters()).map(p -> p.getName()).toArray(String[]::new);

                        if (method.getAnnotation(PostMapping.class) != null) {
                            String path = controllerContextPath + method.getAnnotation(PostMapping.class).value();

                            logger.info("[north] [route]    POST {} -> {}", path, getReadableMethodName(method));
                            postMappings.put(path, new PostDispatcher(controllerInstance, method, parameterNames, method.getParameterTypes(), new JsonConverter()));
                        } else if (method.getAnnotation(RequestMapping.class) != null) {
                            String path = controllerContextPath + method.getAnnotation(RequestMapping.class).value();

                            logger.info("[north] [route] REQUEST {} -> {}", path, getReadableMethodName(method));
                            requestMappings.put(path, new RequestDispatcher(controllerInstance, method, parameterNames, method.getParameterTypes(), new JsonConverter()));
                        } else {
                            String path = controllerContextPath + method.getAnnotation(PutMapping.class).value();

                            logger.info("[north] [route]     PUT {} -> {}", path, getReadableMethodName(method));
                            putMappings.put(path, new PutDispatcher(controllerInstance, method, parameterNames, method.getParameterTypes(), new JsonConverter()));
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
     *
     * @param method 网络请求method
     * @param path   请求路径
     */
    public static MethodDispatcher findDispatcher(String method, String path) {
        MethodDispatcher dispatcher = null;
        switch (method) {
            case "get":
                dispatcher = getMappings.get(path);
                break;
            case "post":
                dispatcher = postMappings.get(path);
                break;
            case "put":
                dispatcher = putMappings.get(path);
                break;
            case "delete":
                dispatcher = deleteMappings.get(path);
                break;
        }

        // @RequestMapping 如果和普通 get/post/put/delete 重复，那么优先走普通 mapping
        if (dispatcher != null) {
            return dispatcher;
        }

        // 返回 requestMappings 的处理器，但也可能路径还是不存在，最后还是返回 null，匹配失败
        return requestMappings.get(path);
    }
}
