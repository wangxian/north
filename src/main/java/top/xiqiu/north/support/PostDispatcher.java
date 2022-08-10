package top.xiqiu.north.support;

import com.google.gson.Gson;
import top.xiqiu.north.core.ModelAndView;
import top.xiqiu.north.core.RouteDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * PostMapping 注解处理
 */
public class PostDispatcher implements RouteDispatcher {

    private final Object instance;
    private final Method method;
    private final Class<?>[] parameterClasses;
    private final Gson postParameter;

    public PostDispatcher(Object instance, Method method, Class<?>[] parameterClasses, Gson postParameter) {
        this.instance         = instance;
        this.method           = method;
        this.parameterClasses = parameterClasses;
        this.postParameter    = postParameter;
    }

    @Override
    public ModelAndView invoke(HttpServletRequest request, HttpServletResponse response) throws IOException, ReflectiveOperationException {
        Object[] arguments = new Object[this.parameterClasses.length];

        for (int i = 0; i < this.parameterClasses.length; i++) {
            Class<?> parameterClass = this.parameterClasses[i];
            if (parameterClass == HttpServletRequest.class) {
                arguments[i] = request;
            } else if (parameterClass == HttpServletResponse.class) {
                arguments[i] = response;
            } else if (parameterClass == HttpSession.class) {
                arguments[i] = request.getSession();
            } else {
                // 解析 post 参数为 bean，注入实参
                arguments[i] = this.postParameter.fromJson(request.getReader(), parameterClass);
            }
        }

        return (ModelAndView) this.method.invoke(this.instance, arguments);
    }
}
