package top.xiqiu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.North;
import top.xiqiu.north.annotation.Component;

import javax.annotation.PostConstruct;

@Component
public class WebConfig {
    /**
     * logger
     **/
    private final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @PostConstruct
    public void testAbc() {
        // 拦截器其一
        North.interceptor(new WebURLInterceptor());

        // 拦截器其二
        // North.interceptor(new URLInterceptorAdapter() {
        //     @Override
        //     public boolean preHandle(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //         logger.info(":: 拦截器执行了 - 其二 ～");
        //         return super.preHandle(req, resp);
        //     }
        // });

        System.out.println("---------- 这里是 @Component WebConfig.testAbc");
    }

    @PostConstruct
    public void testDef() {
        System.out.println("---------- 这里是 @Component WebConfig.testDef");
    }
}
