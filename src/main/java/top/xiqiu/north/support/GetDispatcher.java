package top.xiqiu.north.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;

/**
 * GetMapping 注解处理
 */
public class GetDispatcher implements MethodDispatcher {

    private final Object instance;
    private final Method method;
    private final MethodParameter[] methodParameters;

    public GetDispatcher(Object instance, Method method, MethodParameter[] methodParameters) {
        this.instance         = instance;
        this.method           = method;
        this.methodParameters = methodParameters;
    }

    @Override
    public Object invoke(HttpServletRequest request, HttpServletResponse response) throws ReflectiveOperationException {
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
                throw new RuntimeException("invalid parameter class type: " + parameterClass);
            }
        }

        return this.method.invoke(this.instance, arguments);
    }
}
