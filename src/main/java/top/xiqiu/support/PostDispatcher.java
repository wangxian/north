package top.xiqiu.support;

import top.xiqiu.core.ModelAndView;
import top.xiqiu.core.RouteDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PostDispatcher implements RouteDispatcher {
    @Override
    public ModelAndView invoke(HttpServletRequest request, HttpServletResponse response) throws IOException, ReflectiveOperationException {
        return null;
    }
}
