package top.xiqiu.north.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.annotation.DeleteMapping;
import top.xiqiu.north.annotation.GetMapping;
import top.xiqiu.north.annotation.PostMapping;
import top.xiqiu.north.annotation.PutMapping;
import top.xiqiu.north.support.DeleteDispatcher;
import top.xiqiu.north.support.GetDispatcher;
import top.xiqiu.north.support.PostDispatcher;
import top.xiqiu.north.support.PutDispatcher;

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
                            String path = method.getAnnotation(GetMapping.class).value();

                            logger.debug("GET {} => {}", path, method);
                            getMappings.put(path, new GetDispatcher(controllerInstance, method, parameterNames, method.getParameterTypes()));
                        } else {
                            String path = method.getAnnotation(DeleteMapping.class).value();

                            logger.debug("DELETE {} => {}", path, method);
                            deleteMappings.put(path, new DeleteDispatcher(controllerInstance, method, parameterNames, method.getParameterTypes()));
                        }
                    } else if (method.getAnnotation(PostMapping.class) != null || method.getAnnotation(PutMapping.class) != null) {
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
                            String path = method.getAnnotation(PostMapping.class).value();

                            logger.debug("POST {} => {}", path, method.getName());
                            postMappings.put(path, new PostDispatcher(controllerInstance, method, parameterNames, method.getParameterTypes(), new JsonConverter()));
                        } else {
                            String path = method.getAnnotation(PutMapping.class).value();

                            logger.debug("PUT {} => {}", path, method.getName());
                            putMappings.put(path, new PutDispatcher(controllerInstance, method, parameterNames, method.getParameterTypes(), new JsonConverter()));
                        }
                    }
                }
            } catch (ReflectiveOperationException e) {
                throw new NorthException(e);
            }
        }
    }

    public static Map<String, GetDispatcher> getGetMappings() {
        return getMappings;
    }

    public static Map<String, PostDispatcher> getPostMappings() {
        return postMappings;
    }

    public static Map<String, PutDispatcher> getPutMappings() {
        return putMappings;
    }

    public static Map<String, DeleteDispatcher> getDeleteMappings() {
        return deleteMappings;
    }
}
