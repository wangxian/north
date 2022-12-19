package top.xiqiu.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.support.URLInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截 URL 网络请求
 */
public class WebURLInterceptor extends URLInterceptorAdapter {
    /**
     * logger
     **/
    private final Logger logger = LoggerFactory.getLogger(WebURLInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        logger.info(":: 拦截器执行了 - 其一 ～");

        if (req.getParameter("id") != null && Integer.parseInt(req.getParameter("id")) > 100) {
            resp.setHeader("content-type", "text/plain;charset=utf8");
            resp.getWriter().write("哈哈，你被拦截了～");
            return false;
        }

        // 其它规则 ...

        return true;
    }
}
