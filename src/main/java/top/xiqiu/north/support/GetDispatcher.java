package top.xiqiu.north.support;

import top.xiqiu.north.core.ModelAndView;
import top.xiqiu.north.core.RouteDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GetDispatcher implements RouteDispatcher {
    @Override
    public ModelAndView invoke(HttpServletRequest request, HttpServletResponse response) throws IOException, ReflectiveOperationException {
        return null;
    }
}
