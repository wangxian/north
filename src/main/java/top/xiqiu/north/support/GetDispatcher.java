package top.xiqiu.north.support;

import top.xiqiu.north.core.ModelAndView;
import top.xiqiu.north.core.RouteDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * GetMapping 注解处理
 */
public class GetDispatcher implements RouteDispatcher {

    private final Object instance;
    private final Method method;
    private final String[] parameterNames;
    private final Class<?>[] parameterClasses;

    public GetDispatcher(Object instance, Method method, String[] parameterNames, Class<?>[] parameterClasses) {
        this.instance         = instance;
        this.method           = method;
        this.parameterNames   = parameterNames;
        this.parameterClasses = parameterClasses;
    }

    private String getOrDefault(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return value != null ? value : defaultValue;
    }

    @Override
    public ModelAndView invoke(HttpServletRequest request, HttpServletResponse response) throws IOException, ReflectiveOperationException {
        Object[] arguments = new Object[this.parameterClasses.length];
        for (int i = 0; i < this.parameterClasses.length; i++) {
            String parameterName = this.parameterNames[i];
            Class<?> parameterClass = this.parameterClasses[i];

            if (parameterClass == HttpServletRequest.class) {
                arguments[i] = request;
            } else if (parameterClass == HttpServletResponse.class) {
                arguments[i] = response;
            } else if (parameterClass == HttpSession.class) {
                arguments[i] = request.getSession();
            } else if (parameterClass == int.class) {
                arguments[i] = Integer.valueOf(getOrDefault(request, parameterName, "0"));
            } else if (parameterClass == long.class) {
                arguments[i] = Long.valueOf(getOrDefault(request, parameterName, "0"));
            } else if (parameterClass == boolean.class) {
                arguments[i] = Boolean.valueOf(getOrDefault(request, parameterName, "0"));
            } else if (parameterClass == String.class) {
                arguments[i] = String.valueOf(getOrDefault(request, parameterName, "0"));
            } else {
                throw new RuntimeException("miss handler for type: " + parameterClass);
            }
        }

        return (ModelAndView) this.method.invoke(this.instance, arguments);
    }
}