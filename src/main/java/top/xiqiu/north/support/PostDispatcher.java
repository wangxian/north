package top.xiqiu.north.support;

import top.xiqiu.north.core.JsonConverter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * PostMapping 注解处理
 */
public class PostDispatcher implements MethodDispatcher {

    private final Object instance;
    private final Method method;
    private final String[] parameterNames;
    private final Class<?>[] parameterClasses;
    private final JsonConverter postParameter;

    public PostDispatcher(Object instance, Method method, String[] parameterNames, Class<?>[] parameterClasses, JsonConverter postParameter) {
        this.instance         = instance;
        this.method           = method;
        this.parameterNames   = parameterNames;
        this.parameterClasses = parameterClasses;
        this.postParameter    = postParameter;
    }

    private String getOrDefault(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return value != null ? value : defaultValue;
    }

    @Override
    public Object invoke(HttpServletRequest request, HttpServletResponse response) throws IOException, ReflectiveOperationException {
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
            } else if (parameterClass == Integer.class) {
                try {
                    arguments[i] = Integer.valueOf(getOrDefault(request, parameterName, "0"));
                } catch (NumberFormatException e) {
                    arguments[i] = Integer.valueOf(0);
                }
            } else if (parameterClass == Long.class) {
                try {
                    arguments[i] = Long.valueOf(getOrDefault(request, parameterName, "0"));
                } catch (NumberFormatException e) {
                    arguments[i] = Long.valueOf(0);
                }
            } else if (parameterClass == Float.class) {
                try {
                    arguments[i] = Float.valueOf(getOrDefault(request, parameterName, "0"));
                } catch (NumberFormatException e) {
                    arguments[i] = Float.valueOf(0);
                }
            } else if (parameterClass == Double.class) {
                try {
                    arguments[i] = Double.valueOf(getOrDefault(request, parameterName, "0"));
                } catch (NumberFormatException e) {
                    arguments[i] = Double.valueOf(0);
                }
            } else if (parameterClass == Boolean.class) {
                arguments[i] = Boolean.valueOf(getOrDefault(request, parameterName, "0"));
            } else if (parameterClass == String.class) {
                arguments[i] = String.valueOf(getOrDefault(request, parameterName, ""));
            } else {
                // 读取 post json 数据
                BufferedReader reader = request.getReader();
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }

                // 解析 post 参数为 bean，注入实参
                arguments[i] = this.postParameter.parse(body.toString(), parameterClass);
            }
        }

        return this.method.invoke(this.instance, arguments);
    }
}
