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

public class North {

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
     * 启动 Webapp
     *
     * @param mainAppClass
     */
    public static void start(Class<?> mainAppClass) {
        // 基本目录
        APP_CLASS_PATH = mainAppClass.getProtectionDomain().getCodeSource().getLocation().getPath();

        // 在 fatjar 下运行
        if (APP_CLASS_PATH.endsWith(".jar")) {
            // APP_CLASS_PATH = APP_CLASS_PATH.substring(0, APP_CLASS_PATH.lastIndexOf("/"));
            isAppRunInJar  = true;
        }

        // web.xml 的位置
        String webXmlPath = null;
        try {
            webXmlPath = mainAppClass.getClassLoader().getResource("WEB-INF/web.xml").toString();
        } catch (NullPointerException e) {
        }
        // System.out.println(APP_CLASS_PATH + "\n" + webXmlPath);

        // 默认使用 Tomcat 容器
        Tomcat tomcat = new Tomcat();

        // 设置基础目录，为了安全，指定临时目录
        tomcat.setBaseDir(System.getProperty("java.io.tmpdir") + "north.tomcat");
        System.out.println("java.io.tmpdir=" + System.getProperty("java.io.tmpdir") + "north.tomcat");

        // Set port, default 8080
        tomcat.setPort(config().getIntOrDefault("north.server.port", 8080));

        // Set doc base
        tomcat.getHost().setAppBase(DOC_BASE);

        // Disable auto add web.xml
        tomcat.setAddDefaultWebXmlToWebapp(false);

        // 创建 webapp
        Context context = tomcat.addWebapp(DEFAULT_CONTEXT_PATH, DOC_BASE);

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

        // 开发模式及 fatjar 运行、静态文件处理
        if (isAppRunInJar) {
            // classes
            resources.addJarResources(new JarResourceSet(resources, "/WEB-INF/classes", APP_CLASS_PATH, "/"));

            // 处理静态文件 /static
            resources.addJarResources(new JarResourceSet(resources, "/static", APP_CLASS_PATH, "/static"));
        } else {
            // classes
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", APP_CLASS_PATH, "/"));

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

        // Start Tomcat embedded server
        tomcat.getConnector();
        try {
            tomcat.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }

        tomcat.getServer().await();
    }

    /**
     * WebApp ClassPath
     */
    public static String getClassPath() {
        return APP_CLASS_PATH;
    }

    /**
     * WebApp 运行目录
     * 说明：非fatjar运行和getClassPath结果一致，fatjar运行不包括尾部的xxx.jar
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
