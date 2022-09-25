package top.xiqiu.north.support;

import top.xiqiu.north.core.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class URLInterceptorAdapter {
    /**
     * This implementation always returns true.
     */
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        return true;
    }

    /**
     * This implementation is empty.
     */
    public void postHandle(HttpServletRequest req, HttpServletResponse resp, ModelAndView modelAndView) throws Exception {
    }
}
