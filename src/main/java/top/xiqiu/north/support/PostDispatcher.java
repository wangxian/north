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
    private final MethodParameter[] methodParameters;

    public PostDispatcher(Object instance, Method method, MethodParameter[] methodParameters) {
        this.instance         = instance;
        this.method           = method;
        this.methodParameters = methodParameters;
    }

    @Override
    public Object invoke(HttpServletRequest request, HttpServletResponse response) throws IOException, ReflectiveOperationException {
        Object[] arguments = new Object[this.methodParameters.length];

        for (int i = 0; i < this.methodParameters.length; i++) {
            Class<?> parameterClass = this.methodParameters[i].getClassType();

            if (parameterClass == HttpServletRequest.class) {
                arguments[i] = request;
            } else if (parameterClass == HttpServletResponse.class) {
                arguments[i] = response;
            } else if (parameterClass == HttpSession.class) {
                arguments[i] = request.getSession();
            } else if (parameterClass == Integer.class) {
                try {
                    arguments[i] = Integer.valueOf(getOrDefault(request, this.methodParameters[i], "0"));
                } catch (NumberFormatException e) {
                    arguments[i] = 0;
                }
            } else if (parameterClass == Long.class) {
                try {
                    arguments[i] = Long.valueOf(getOrDefault(request, this.methodParameters[i], "0"));
                } catch (NumberFormatException e) {
                    arguments[i] = 0L;
                }
            } else if (parameterClass == Float.class) {
                try {
                    arguments[i] = Float.valueOf(getOrDefault(request, this.methodParameters[i], "0"));
                } catch (NumberFormatException e) {
                    arguments[i] = (float) 0;
                }
            } else if (parameterClass == Double.class) {
                try {
                    arguments[i] = Double.valueOf(getOrDefault(request, this.methodParameters[i], "0"));
                } catch (NumberFormatException e) {
                    arguments[i] = (double) 0;
                }
            } else if (parameterClass == Boolean.class) {
                arguments[i] = Boolean.valueOf(getOrDefault(request, this.methodParameters[i], "0"));
            } else if (parameterClass == String.class) {
                arguments[i] = String.valueOf(getOrDefault(request, this.methodParameters[i], ""));
            } else {
                // 读取 post json 数据
                BufferedReader reader = request.getReader();
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }

                // 解析 post 参数为 bean，注入实参
                arguments[i] = new JsonConverter().parse(body.toString(), parameterClass);
            }
        }

        return this.method.invoke(this.instance, arguments);
    }
}
