package top.xiqiu.north;

import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.jasper.servlet.JasperInitializer;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.core.*;
import top.xiqiu.north.support.PostConstructProcessor;
import top.xiqiu.north.support.URLInterceptorAdapter;

import java.io.File;
import java.util.List;

/**
 * North App main class
 */
public class North {

    /**
     * logger
     **/
    private static final Logger logger = LoggerFactory.getLogger(North.class);

    /**
     * Tomcat server + Context
     */
    private static Tomcat tomcat;
    private static StandardContext context;

    /**
     * Context 配置
     */
    private static final String DEFAULT_CONTEXT_PATH = "";
    private static final String DOC_BASE = ".";

    /**
     * 应用程序基本目录
     */
    private static String APP_CLASS_PATH;

    /**
     * WebTester 是否在 fatjar 下运行
     */
    public static boolean isAppRunInJar = false;

    /**
     * 记录启动时间（计算启动耗时）
     */
    private static long _startTime = 0;

    /**
     * APP 主入口类
     */
    private static Class<?> _mainAppClass;

    /**
     * 启动 Webapp
     *
     * @param mainAppClass 主入口class
     * @param args         启动参数
     */
    public static void start(Class<?> mainAppClass, String[] args) {
        if (tomcat != null) {
            logger.error("STARTUP ERROR, Tomcat server is already running.");
            return;
        }

        // North app instance
        North north = new North();

        // APP 主入口类
        North._mainAppClass = mainAppClass;

        // 初始化配制信息(单例，只需要初始化一次)
        AppConfig.of(args);

        // 做一些 Server 启动前的准备
        north._prepareServer();

        // 设置 404 / 500 错误页面
        north._setErrorPage();

        // 启动内置web服务器
        north._startServer();
    }

    /**
     * 准备 web server 配置
     */
    @SuppressWarnings("CommentedOutCode")
    private void _prepareServer() {
        _startTime = System.currentTimeMillis();

        logger.info("[north] north.env = {}", config().get("north.env", "\"\""));

        // 基本目录，fatjar 路径是 xxx/target/xxx.jar
        APP_CLASS_PATH = _mainAppClass.getProtectionDomain().getCodeSource().getLocation().getPath();
        logger.info("[north] app.classpath = {}", APP_CLASS_PATH);

        // 在 fatjar 下运行
        if (APP_CLASS_PATH.endsWith(".jar")) {
            isAppRunInJar = true;
        }

        if (isAppRunInJar) {
            logger.info("[north] north.version = {}", config().getNorthVersion());
        }

        // 默认使用 Tomcat 容器
        tomcat = new Tomcat();

        // 屏蔽 tomcat 启动日志
        java.util.logging.Logger.getLogger("org.apache").setLevel(java.util.logging.Level.WARNING);

        int port = config().getInt("server.port", 8080);

        // 设置基础目录，为了安全，指定临时目录
        String tmpdir = System.getProperty("java.io.tmpdir");
        if (!tmpdir.endsWith("/")) {
            tmpdir = tmpdir + "/";
        }
        tmpdir = tmpdir + "north-tomcat-" + port + "-" + System.currentTimeMillis();
        tomcat.setBaseDir(tmpdir);
        logger.debug("[north] server.tmpdir={}", tmpdir);

        // Set port, default 8080
        tomcat.setPort(port);
        // tomcat.getHost().setAutoDeploy(false);

        // 设置 tomcat 运行参数设置
        // 当所有线程都在使用时，建立连接的请求的等待队列长度，默认100
        // tomcat.getConnector().setProperty("acceptCount", "1000");
        // 最大线程数，默认200
        // tomcat.getConnector().setProperty("maxThreads", "1000");
        // 允许最大连接数，当达到临界值时，系统可能会基于accept-count继续接受连接，默认10000
        // tomcat.getConnector().setProperty("maxConnections", "10000");

        // Set doc base
        tomcat.getHost().setAppBase(DOC_BASE);

        // context = tomcat.addWebapp(DEFAULT_CONTEXT_PATH, DOC_BASE);
        // context = tomcat.addContext(DEFAULT_CONTEXT_PATH, DOC_BASE);

        context = new StandardContext();
        context.setPath(DEFAULT_CONTEXT_PATH);
        context.setDocBase(tmpdir);

        // 热加载，类编译后，自动reload，刷新浏览器即可查看效果，
        // 在非 fatjar 下默认启用
        if (!isAppRunInJar) {
            context.setReloadable(true);
        }

        // 资源处理
        WebResourceRoot resources = new StandardRoot(context);

        // 是否支持 jsp，不使用 jsp 作为模版引擎的时候，可以不设置支持 jsp
        if ("jsp".equals(North.config().get("north.view-engine", "no"))) {
            // 支持 jsp 后缀
            _supportJsp();

            // 设置 jsp templates 映射目录
            if (isAppRunInJar) {
                resources.addJarResources(new JarResourceSet(resources, "/WEB-INF/templates", APP_CLASS_PATH, "/templates"));
            } else {
                // 检测文件是否存在，开发环境检测即可，给出友好的提示
                if (!new File(APP_CLASS_PATH + "templates").exists()) {
                    throw new RuntimeException("发生错误：使用 jsp 作为模版引擎，必须存在 classpath:templates 目录");
                }

                resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/templates", APP_CLASS_PATH, "/templates"));
            }
        }

        // 支持 tomcat 原生的注解
        if (isAppRunInJar) {
            resources.addJarResources(new JarResourceSet(resources, "/WEB-INF/classes", APP_CLASS_PATH, "/"));
        } else {
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", APP_CLASS_PATH, "/"));
        }

        // 添加资源
        context.setResources(resources);

        // 设置上传临时目录
        context.setCreateUploadTargets(true);

        // Set tld skip, 解决/屏蔽 tomcat 启动时 TLDs warning
        _setTldSkip();

        // add servlet
        _addServlet();

        // 添加启动监听
        _addLifecycleListener();

        // 添加 context 到 host
        tomcat.getHost().addChild(context);
    }

    /**
     * Setting ErrorPage, 404, 500
     */
    private void _setErrorPage() {
        String errorPage404 = config().get("north.error-page-404", "");
        if (!"".equals(errorPage404)) {
            ErrorPage page404 = new ErrorPage();
            page404.setErrorCode(404);
            page404.setLocation(errorPage404);

            context.addErrorPage(page404);
            logger.info("[north] setting error page 404 = {}", errorPage404);
        }

        String errorPage500 = config().get("north.error-page-500", "");
        if (!"".equals(errorPage500)) {
            ErrorPage page500 = new ErrorPage();
            page500.setErrorCode(500);
            page500.setLocation(errorPage500);

            context.addErrorPage(page500);
            logger.info("[north] setting error page 404 = {}", errorPage500);
        }
    }

    /**
     * Add servlets
     */
    private void _addServlet() {
        // Default servlet
        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("default");
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        defaultServlet.setOverridable(true);
        context.addChild(defaultServlet);

        // Dispatch all web servlet
        Tomcat.addServlet(context, "north-dispatcher", new DispatcherServlet());
        context.addServletMappingDecoded("/", "north-dispatcher");

        // Serve static files & favicon.ico
        Tomcat.addServlet(context, "static-files", new FileServerServlet());
        context.addServletMappingDecoded("/static/*", "static-files");
        context.addServletMappingDecoded("/favicon.ico", "static-files");
    }

    /**
     * Set tld skip
     */
    private void _setTldSkip() {
        // Set scanBootstrapClassPath="true" depending on
        // exactly how your far JAR is packaged / structured
        StandardJarScanner standardJarScanner = new StandardJarScanner();
        standardJarScanner.setScanBootstrapClassPath(true);

        // 解决/屏蔽 tomcat 启动时 TLDs warning
        StandardJarScanFilter filter = new StandardJarScanFilter();
        filter.setTldSkip("*.jar");
        standardJarScanner.setJarScanFilter(filter);
        context.setJarScanner(standardJarScanner);
    }

    /**
     * 支持 jsp
     */
    private void _supportJsp() {
        // Support *.jsp
        Wrapper jspServlet = context.createWrapper();

        jspServlet.setName("jsp");
        jspServlet.setServletClass("org.apache.jasper.servlet.JspServlet");
        jspServlet.addInitParameter("fork", "false");
        jspServlet.setLoadOnStartup(3);

        context.addChild(jspServlet);
        context.addServletMappingDecoded("*.jsp", "jsp");
        context.addServletMappingDecoded("*.jspx", "jsp");
        context.addServletContainerInitializer(new JasperInitializer(), null);
    }

    /**
     * 添加监听
     */
    @SuppressWarnings("CommentedOutCode")
    private void _addLifecycleListener() {
        // context.addApplicationListener(WsContextListener.class.getName());
        // context.addLifecycleListener(new Tomcat.FixContextListener());

        /*
         * 添加生命周期监听
         * 用于解析扫描 web.xml、@WebFilter、@WebServlet 等注解
         */
        context.addLifecycleListener(new ContextConfig());

        // 监听 Server 启动事件
        // noinspection Convert2Lambda
        context.addLifecycleListener(new LifecycleListener() {
            @Override
            public void lifecycleEvent(LifecycleEvent event) {
                // logger.info("收到 LifecycleListener 事件 = {}", event.getType());
                switch (event.getType()) {
                    case Lifecycle.START_EVENT:
                        onStart(event);
                        break;
                    case Lifecycle.AFTER_START_EVENT:
                        onAfterStart(event);
                        break;
                    case Lifecycle.BEFORE_START_EVENT:
                        onBeforeStart(event);
                        break;
                }
            }
        });
    }

    /**
     * 启动 web server
     */
    private void _startServer() {
        Connector connector = new Connector();
        connector.setPort(config().getInt("server.port", 8080));
        connector.setURIEncoding("UTF-8");
        connector.setThrowOnFailure(true);
        tomcat.getService().addConnector(connector);
        tomcat.setConnector(connector);

        try {
            tomcat.start();
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            logger.error("启动 Tomcat server 失败={}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * 启动之前触发
     */
    @SuppressWarnings("unused")
    private void onBeforeStart(LifecycleEvent event) {
        // 扫描需要预处理的类并处理相关注解
        final List<Class<?>> classes = ScanClassWithAnnotations.findClasses(_mainAppClass.getPackageName());
        // logger.debug("[north] 扫描到的类 = {}", classes);

        // 处理 @Controller 注解
        ScanClassWithAnnotations.scanAndStoreControllers(classes);

        // 处理 @Bean 注解 + 初始化
        ScanClassWithAnnotations.scanAndStoreBeans(classes);

        // 处理 @Service 注解 + 初始化
        ScanClassWithAnnotations.scanAndStoreService(classes);

        // 所有的组件 @Component
        final List<Class<?>> components = ScanClassWithAnnotations.scanComponents(classes);

        // 处理 @PostConstruct 注解，并执行
        PostConstructProcessor.invoke(components);
    }

    /**
     * 启动时触发
     */
    @SuppressWarnings("unused")
    private void onStart(LifecycleEvent event) {
        // Leave startup message
        logger.info("[north] startup.time.cost={}ms | startup success at http://{}:{}/",
                    System.currentTimeMillis() - _startTime,
                    config().get("server.host", "0.0.0.0"),
                    config().getInt("server.port", 8080));
    }

    /**
     * 启动之后触发
     */
    @SuppressWarnings("unused")
    private void onAfterStart(LifecycleEvent event) {
        // 预处理控制器 xxxMapping 注解
        RouteHandler.processMappings();
    }

    /**
     * Web app ClassPath
     * **说明：**
     * - 路径为 ./xxx/target/classes/ 或 ../xxx/target/xxx.jar
     * - 非 fatjar 运行和 getWorkingDirectory() 结果一样，fatjar运行括后缀 xxx.jar
     */
    public static String getClassPath() {
        return APP_CLASS_PATH;
    }

    /**
     * 运行目录(注意：路径以 / 结尾)
     * 说明：返回应用程序工作的目录
     */
    public static String getWorkingDirectory() {
        if (isAppRunInJar) {
            return APP_CLASS_PATH.substring(0, APP_CLASS_PATH.lastIndexOf("/"));
        }

        return APP_CLASS_PATH;
    }

    /**
     * 读取 AppConfig 配置
     */
    public static AppConfig config() {
        return AppConfig.of();
    }

    /**
     * 注册一个 interceptor
     */
    public static void interceptor(URLInterceptorAdapter interceptor) {
        ScanClassWithAnnotations.addStoredInterceptors(interceptor);
    }

    /**
     * 获取 tomcat context
     */
    public static StandardContext getContext() {
        return context;
    }
}
