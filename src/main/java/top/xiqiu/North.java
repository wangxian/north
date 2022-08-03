package top.xiqiu;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.jasper.servlet.JasperInitializer;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.core.AppConfig;

import java.util.Objects;

public class North {

    public static final Logger logger = LoggerFactory.getLogger(North.class);

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
     * WebApp 是否在 fatjar 下运行
     */
    private static boolean isAppRunInJar = false;

    /**
     * web.xml 位置
     */
    private static String webXmlPath;

    /**
     * 启动 Webapp
     *
     * @param mainAppClass 主入口class
     */
    public static void start(Class<?> mainAppClass) {
        // 基本目录，fatjar 路径是 xxx/target/xxx.jar
        APP_CLASS_PATH = mainAppClass.getProtectionDomain().getCodeSource().getLocation().getPath();

        // 在 fatjar 下运行
        if (APP_CLASS_PATH.endsWith(".jar")) {
            isAppRunInJar = true;
        }

        // web.xml 的位置
        webXmlPath = Objects.requireNonNull(mainAppClass.getClassLoader().getResource("WEB-INF/web.xml")).toString();
        logger.info("web.xml absolute path={}", webXmlPath);

        _prepareServer();
        _supportJsp();
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

        // 设置基础目录，为了安全，指定临时目录
        String tmpdir = System.getProperty("java.io.tmpdir") + "north.tomcat." + System.currentTimeMillis();
        tomcat.setBaseDir(tmpdir);
        logger.debug("server.tmpdir={}", tmpdir);

        // Set port, default 8080
        tomcat.setPort(config().getIntOrDefault("server.port", 8080));
        tomcat.getConnector();

        // Set doc base
        tomcat.getHost().setAppBase(DOC_BASE);

        // Disable auto add web.xml
        tomcat.setAddDefaultWebXmlToWebapp(false);

        // 创建 webapp
        context = tomcat.addWebapp(DEFAULT_CONTEXT_PATH, DOC_BASE);

        // Disable auto add web.xml 后必须添加 addLifecycleListener
        // 否则静态资源 + jsp解析有问题
        ((StandardContext) context).setDefaultWebXml(webXmlPath);
        context.addLifecycleListener(new Tomcat.DefaultWebXmlListener());

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
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/templates", APP_CLASS_PATH, "/templates/"));

            // 处理静态文件 /static
            resources.addPreResources(new DirResourceSet(resources, "/static", APP_CLASS_PATH, "/static/"));
        }
        context.setResources(resources);

        // Set scanBootstrapClassPath="true" depending on
        // exactly how your far JAR is packaged / structured
        StandardJarScanner standardJarScanner = new StandardJarScanner();
        standardJarScanner.setScanBootstrapClassPath(true);
        context.setJarScanner(standardJarScanner);

        // 解决/屏蔽 tomcat 启动时 TLDs warning
        StandardJarScanFilter filter = new StandardJarScanFilter();
        filter.setTldSkip("*.jar");
        context.getJarScanner().setJarScanFilter(filter);
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
            logger.info("Startup success at http://{}:{}/", "127.0.0.1", config().getIntOrDefault("port", 8080));

            tomcat.getServer().await();
        } catch (LifecycleException e) {
            logger.error("启动 Tomcat server 失败={}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * WebApp ClassPath
     * <p>
     * 说明：
     * - 路径为 .../target/classes/ 或 .../target/xxx.jar
     * - 非 fatjar 运行和 getClassPath() 结果一样，fatjar运行不包括尾部的 xxx.jar
     */
    public static String getClassPath() {
        return APP_CLASS_PATH;
    }

    /**
     * WebApp 运行目录(注意：路径以 / 结尾)
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
        return AppConfig.getInstance();
    }
}
