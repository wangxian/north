package top.xiqiu.north;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
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

import java.util.List;

public class North {

    /**
     * logger
     **/
    private static final Logger logger = LoggerFactory.getLogger(North.class);

    /**
     * tomcat server + context
     */
    private static Tomcat tomcat;
    private static Context context;

    /**
     * context 配置
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
     * 启动 Webapp
     *
     * @param mainAppClass 主入口class
     */
    public static void start(Class<?> mainAppClass) {
        // 基本目录，fatjar 路径是 xxx/target/xxx.jar
        APP_CLASS_PATH = mainAppClass.getProtectionDomain().getCodeSource().getLocation().getPath();
        logger.info("[north] app.classpath = {}", APP_CLASS_PATH);

        // 在 fatjar 下运行
        if (APP_CLASS_PATH.endsWith(".jar")) {
            isAppRunInJar = true;
            System.out.println(mainAppClass.getProtectionDomain().getCodeSource().getLocation().toString());
        }

        if (isAppRunInJar) {
            logger.info("[north] north.version = {}", config().getNorthVersion());
        }

        // 扫描需要预处理的类并处理相关注解
        final List<Class<?>> classes = ScanClassWithAnnotations.findClasses(mainAppClass.getPackageName());
        logger.info("[north] 扫描到的类 = {}", classes);

        // 处理 @Controller 注解
        ScanClassWithAnnotations.scanAndStoreControllers(classes);

        // 预处理 xxxMapping 注解
        RouteHandler.processMappings();

        // 做一些启动前的准备
        _prepareServer();

        // 是否支持 jsp，不使用 jsp 作为模版引擎的时候，可以不设置支持 jsp
        if ("jsp".equals(North.config().get("north.view-engine", "pebble"))) {
            _supportJsp();
        }

        // 启动内置web服务器
        _startServer();
    }

    /**
     * 准备 web server 配置
     */
    private static void _prepareServer() {
        // 默认使用 Tomcat 容器
        tomcat = new Tomcat();

        // 屏蔽 tomcat 启动日志
        java.util.logging.Logger.getLogger("org.apache").setLevel(java.util.logging.Level.WARNING);

        int port = config().getInt("server.port", 8080);

        // 设置基础目录，为了安全，指定临时目录
        String tmpdir = System.getProperty("java.io.tmpdir") + "north-tomcat-" + port + "-" + System.currentTimeMillis();
        tomcat.setBaseDir(tmpdir);
        logger.debug("[north] server.tmpdir={}", tmpdir);

        // Set port, default 8080
        tomcat.setPort(port);
        tomcat.getConnector();

        // 设置 tomcat 运行参数设置
        // 当所有线程都在使用时，建立连接的请求的等待队列长度，默认100
        // tomcat.getConnector().setProperty("acceptCount", "1000");
        // 最大线程数，默认200
        // tomcat.getConnector().setProperty("maxThreads", "1000");
        // 允许最大连接数，当达到临界值时，系统可能会基于accept-count继续接受连接，默认10000
        // tomcat.getConnector().setProperty("maxConnections", "10000");

        // Set doc base
        tomcat.getHost().setAppBase(DOC_BASE);

        // 创建 context,
        // context = tomcat.addWebapp(DEFAULT_CONTEXT_PATH, DOC_BASE);
        context = tomcat.addContext(DEFAULT_CONTEXT_PATH, DOC_BASE);

        // 热加载，类编译后，自动reload，刷新浏览器即可查看效果，
        // 在非 fatjar 下默认启用
        if (!isAppRunInJar) {
            context.setReloadable(true);
        }

        // 资源处理
        WebResourceRoot resources = new StandardRoot(context);

        // 设置 tomcat 运行环境
        // 开发模式及 fatjar 运行、静态文件处理
        if (isAppRunInJar) {
            // classes
            resources.addJarResources(new JarResourceSet(resources, "/WEB-INF/classes", APP_CLASS_PATH, "/"));

            // templates
            resources.addJarResources(new JarResourceSet(resources, "/WEB-INF/templates", APP_CLASS_PATH, "/templates"));

            // 处理静态文件 /static
            resources.addJarResources(new JarResourceSet(resources, "/static", APP_CLASS_PATH, "/static"));
        } else {
            // classes
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", APP_CLASS_PATH, "/"));

            // templates
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/templates", APP_CLASS_PATH, "/templates"));

            // 处理静态文件 /static
            resources.addPreResources(new DirResourceSet(resources, "/static", APP_CLASS_PATH, "/static"));
        }
        context.setResources(resources);

        // Set scanBootstrapClassPath="true" depending on
        // exactly how your far JAR is packaged / structured
        StandardJarScanner standardJarScanner = new StandardJarScanner();
        standardJarScanner.setScanBootstrapClassPath(true);
        // 解决/屏蔽 tomcat 启动时 TLDs warning
        StandardJarScanFilter filter = new StandardJarScanFilter();
        filter.setTldSkip("*.jar");
        standardJarScanner.setJarScanFilter(filter);
        context.setJarScanner(standardJarScanner);

        // Dispatch all web servlet
        tomcat.addServlet(DEFAULT_CONTEXT_PATH, "north-dispatcher", new DispatcherServlet());
        context.addServletMappingDecoded("/", "north-dispatcher");

        // Serve static files & favicon.ico
        tomcat.addServlet(DEFAULT_CONTEXT_PATH, "static-files", new FileServerServlet());
        context.addServletMappingDecoded("/static/*", "static-files");
        context.addServletMappingDecoded("/favicon.ico", "static-files");

        // ErrorPage
        ErrorPage page404 = new ErrorPage();
        page404.setErrorCode(404);
        page404.setLocation("/404");
        context.addErrorPage(page404);
    }

    /**
     * 支持 jsp
     */
    private static void _supportJsp() {
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
     * 启动 web server
     */
    private static void _startServer() {
        // Start Tomcat embedded server
        try {
            tomcat.start();

            // Leave startup message
            logger.info("[north] startup success at http://{}:{}/",
                        config().get("server.host", "0.0.0.0"),
                        config().getInt("server.port", 8080));

            tomcat.getServer().await();
        } catch (LifecycleException e) {
            logger.error("启动 Tomcat server 失败={}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * WebTester ClassPath
     * <p>
     * 说明：
     * - 路径为 .../target/classes/ 或 .../target/xxx.jar
     * - 非 fatjar 运行和 getClassPath() 结果一样，fatjar运行不包括尾部的 xxx.jar
     */
    public static String getClassPath() {
        return APP_CLASS_PATH;
    }

    /**
     * WebTester 运行目录(注意：路径以 / 结尾)
     * 说明：返回应用程序运行的目录
     */
    public static String getBasePath() {
        if (isAppRunInJar) {
            return APP_CLASS_PATH.substring(0, APP_CLASS_PATH.lastIndexOf("/"));
        }

        return APP_CLASS_PATH;
    }

    /**
     * 读取 AppConfig 配置
     */
    public static AppConfig config() {
        return AppConfig.init();
    }
}
