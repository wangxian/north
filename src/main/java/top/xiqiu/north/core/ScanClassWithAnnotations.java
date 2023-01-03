package top.xiqiu.north.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.annotation.*;
import top.xiqiu.north.support.BeanFactory;
import top.xiqiu.north.support.BeanStoredEntity;
import top.xiqiu.north.support.URLInterceptorAdapter;
import top.xiqiu.north.util.NorthUtils;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ScanClassWithAnnotations {
    /**
     * logger
     **/
    private static final Logger logger = LoggerFactory.getLogger(ScanClassWithAnnotations.class);

    /**
     * 存储 扫描到的控制器(controllers)
     */
    private final static List<Class<?>> storedControllers = new ArrayList<>();

    /**
     * 缓存的 Beans
     */
    private final static List<BeanStoredEntity> storedBeans = new ArrayList<>();

    /**
     * 缓存的 Interceptors
     */
    private final static List<URLInterceptorAdapter> storedInterceptors = new ArrayList<>();

    /**
     * 获取所有的注解的控制器
     */
    public static List<Class<?>> getStoredControllers() {
        return storedControllers;
    }

    /**
     * 查找包下面的类
     *
     * @param basePackageName 包名，如：io.webapp.test
     * @return 找到的类
     */
    public static List<Class<?>> findClasses(String basePackageName) {
        List<Class<?>> classes = new ArrayList<>();
        Enumeration<URL> dirs;

        try {
            String packageDirName = basePackageName.replace(".", "/");
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
        } catch (IOException e) {
            logger.error("failed to get resources: {}", e.getMessage());
            // e.printStackTrace();
            return classes;
        }

        // 遍历所有的资源
        while (dirs.hasMoreElements()) {
            URL url = dirs.nextElement();
            String protocol = url.getProtocol();

            // logger.info("package = {}", url.getFile());

            // protocol 可以是 http/https/file/jar
            // 扫描只需处理 file/jar 即可
            if ("file".equals(protocol)) {
                // 扫描目录下级所有的类
                _getClassesByFilePath(basePackageName, Path.of(url.getFile()), classes);
            } else if ("jar".equals(protocol)) {
                JarFile jarFile;
                try {
                    jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                } catch (IOException e) {
                    logger.error("failed to load jar file", e);
                    continue;
                }

                // 扫描 jar 下级所有的类
                _getClassesByJar(basePackageName, jarFile, classes);
            }

            // logger.info("下级所有的类={}", classes);
        }

        return classes;
    }

    /**
     * 根据文件路径扫描 class
     *
     * @param currentPackageName 当前包名（递归的时候需要）
     * @param filePath           文件路径
     * @param classes            返回的class集合
     */
    private static void _getClassesByFilePath(String currentPackageName, Path filePath, List<Class<?>> classes) {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(filePath)) {
            for (Path path : paths) {
                // 文件名，eg: xxx.class
                String fileName = path.toFile().getName();
                // logger.info("filepath = {}", path.toFile().getPath() + "/" + path.toFile().getName());

                // 如果 path 是一个目录，则递归调用获取下级文件及目录
                if (Files.isDirectory(path)) {
                    _getClassesByFilePath(currentPackageName + "." + fileName, path, classes);
                    continue;
                }

                // 类名，去掉 .class 后缀
                String fullClassName = currentPackageName + "." + fileName.substring(0, fileName.length() - 6);
                Class<?> clazz = NorthUtils.loadClass(fullClassName);

                // 记录加载成功的类
                if (clazz != null) {
                    classes.add(clazz);
                }
            }
        } catch (IOException e) {
            logger.error("failed to read class by file path", e);
            // e.printStackTrace();
        }
    }

    /**
     * 扫描 package 下的所有 class
     *
     * @param currentPackageName 当前包名
     * @param jarFile            jar文件
     * @param classes            返回的class集合
     */
    private static void _getClassesByJar(String currentPackageName, JarFile jarFile, List<Class<?>> classes) {
        String packageDirName = currentPackageName.replace(".", "/");
        final Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文
            final JarEntry jarEntry = entries.nextElement();

            // jar内的文件名
            // 类似：org/slf4j/helpers/Util.class, META-INF/maven/org.slf4j/slf4j-simple/pom.xml, static/404.html
            String name = jarEntry.getName();

            // 如果是以 / 开头的，获取后面的字符串
            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }

            // 过滤一些类型的文件
            if (jarEntry.isDirectory() || !name.startsWith(packageDirName) || !name.endsWith(".class")) {
                continue;
            }

            // logger.info("jar filepath = {}", name);

            String className = name.substring(0, name.length() - 6);
            Class<?> clazz = NorthUtils.loadClass(className.replace("/", "."));

            // 记录加载成功的类
            if (clazz != null) {
                classes.add(clazz);
            }
        }
    }

    /**
     * 扫描所有的控制器
     *
     * @param classes 包下的所有类
     */
    public static void scanAndStoreControllers(List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (clazz.getAnnotation(Controller.class) != null) {
                storedControllers.add(clazz);
            }
        }

        // logger.info("扫描到的控制器 = {}", storedControllers);
    }

    /**
     * 扫描 @Component 注解
     *
     * @param classes 包下的所有类
     */
    public static List<Class<?>> scanComponents(List<Class<?>> classes) {
        return classes.stream().filter(clazz -> clazz.getAnnotation(Component.class) != null).collect(Collectors.toList());
    }

    /**
     * 扫描 @Bean 注解，并初始化
     *
     * @param classes 包下的所有类
     */
    public static void scanAndStoreBeans(List<Class<?>> classes) {
        // noinspection LambdaParameterTypeCanBeSpecified,CodeBlock2Expr
        classes.stream().filter(clazz -> clazz.getAnnotation(Configuration.class) != null).forEach(clazz -> {
            Arrays.stream(clazz.getMethods()).forEach(method -> {
                if (method.getAnnotation(Bean.class) != null) {
                    // 执行对象实例
                    Object instance;

                    try {
                        instance = method.invoke(clazz.getConstructor().newInstance());
                    } catch (ReflectiveOperationException e) {
                        throw new NorthException(e);
                    }

                    storedBeans.add(new BeanStoredEntity(method.getName(), method.getReturnType(), instance));
                }
            });
        });
    }

    /**
     * 扫描 @Service 注解，并初始化
     *
     * @param classes 包下的所有类
     */
    public static void scanAndStoreService(List<Class<?>> classes) {
        // 找到所有的 @Service
        List<Class<?>> services = classes.stream().filter(clazz -> clazz.getAnnotation(Service.class) != null).collect(Collectors.toList());

        // 先处理一遍所有的 Servers，然后再处理 autowired 的注入
        for (Class<?> clazz : services) {
            // 执行对象实例
            Object instance;

            try {
                instance = clazz.getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new NorthException(e);
            }

            // noinspection DuplicatedCode
            String beanName;
            Class<?> beanReturnType;

            // 可能未实现服务接口
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                beanName       = interfaces[0].getName();
                beanReturnType = interfaces[0];
            } else {
                beanName       = clazz.getName();
                beanReturnType = clazz;
            }

            storedBeans.add(new BeanStoredEntity(beanName, beanReturnType, instance));
        }

        // 处理 autowired 的注入，service 相互依赖
        for (Class<?> clazz : services) {
            // noinspection DuplicatedCode
            String beanName;
            Class<?> beanReturnType;

            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                beanName       = interfaces[0].getName();
                beanReturnType = interfaces[0];
            } else {
                beanName       = clazz.getName();
                beanReturnType = clazz;
            }

            // 获得已实例化的 service 对象
            Object clazzInstance = BeanFactory.getBean(beanName, beanReturnType);

            // 注入 Autowired 修饰的属性
            Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
                if (field.getAnnotation(Autowired.class) != null) {
                    Object beanInstance = BeanFactory.getBean(field.getType());
                    if (beanInstance != null) {
                        field.setAccessible(true);
                        try {
                            field.set(clazzInstance, beanInstance);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    /**
     * 获得存储的 beans
     */
    public static List<BeanStoredEntity> getStoredBeans() {
        return storedBeans;
    }

    /**
     * 增加 interceptor
     */
    public static void addStoredInterceptors(URLInterceptorAdapter interceptor) {
        storedInterceptors.add(interceptor);
    }

    /**
     * 获得存储的 Interceptors
     */
    public static List<URLInterceptorAdapter> getStoredInterceptors() {
        return storedInterceptors;
    }
}
