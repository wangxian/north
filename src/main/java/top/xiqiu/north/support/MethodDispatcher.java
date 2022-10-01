package top.xiqiu.north.support;

import top.xiqiu.north.core.NorthException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface MethodDispatcher {
    Object invoke(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ReflectiveOperationException;

    default String getOrDefault(HttpServletRequest request, MethodParameter methodParameter, String defaultValue) {
        String value = request.getParameter(methodParameter.getName());

        // 兼容 pathVariable
        if (value == null) {
            value = (String) request.getAttribute("_path_variable_" + methodParameter.getName());
        }

        // 参数是必需的
        if (value == null && methodParameter.isRequired()) {
            throw new NorthException("method parameter of `" + methodParameter.getName() + "` is required");
        }

        if (methodParameter.getDefaultValue() != null) {
            defaultValue = methodParameter.getDefaultValue();
        }

        return value != null ? value : defaultValue;
    }
}
