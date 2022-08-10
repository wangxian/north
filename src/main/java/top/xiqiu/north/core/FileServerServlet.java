package top.xiqiu.north.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.North;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 文件服务器 for /static/*
 */
public class FileServerServlet extends HttpServlet {
    /**
     * logger
     **/
    private static Logger logger = LoggerFactory.getLogger(FileServerServlet.class);

    /**
     * 静态资源有效期（秒）
     */
    private static final int maxAge = 86400 * 3;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String webFilePath = req.getRequestURI();

        // 处理浏览器自动访问 /favicon.ico 返回404问题
        // 映射到 /static/favicon.ico，这是一个相对简单到做法
        if ("/favicon.ico".equals(webFilePath)) {
            webFilePath = "/static/favicon.ico";
        }

        logger.debug("GET {}", webFilePath);

        webFilePath = webFilePath.substring(1);
        final URL resource = this.getClass().getClassLoader().getResource(webFilePath);
        if (resource == null) {
            resp.sendError(404);
            return;
        }

        // 文件全路径
        String filename = resource.getPath();
        // System.out.println("filename = " + filename);

        // 处理静态资源 304 状态
        // 文件最后修改时间
        FileTime lastModifiedTime;

        // 文件大小
        long fileSize;

        if (North.isAppRunInJar) {
            // in fatjar
            final JarFile jarFile = ((JarURLConnection) resource.openConnection()).getJarFile();
            final JarEntry jarEntry = jarFile.getJarEntry(webFilePath);
            lastModifiedTime = jarEntry.getLastModifiedTime();
            fileSize         = jarEntry.getSize();
        } else {
            // in ide debug
            Path filepath = Path.of(filename);
            lastModifiedTime = Files.getLastModifiedTime(filepath);
            fileSize         = Files.size(filepath);
        }
        // System.out.println("lastModifiedTime = " + lastModifiedTime.toString());

        // 支持客户端 ETag 模式
        final String clientIfNoneMatch = req.getHeader("If-None-Match");
        final String eTag = "\"" + lastModifiedTime.toMillis() + "-" + fileSize + "\"";
        resp.setHeader("ETag", eTag);

        // 客户端上行的最后修改时间
        final String clientModifiedSince = req.getHeader("If-Modified-Since");
        // 设置最后的修改时间
        resp.setHeader("Cache-Control", "max-age=" + maxAge);
        resp.setHeader("Last-Modified", lastModifiedTime.toString());

        // 资源文件如果未修改，则给客户端发送 304状态，不发送内容实体
        if ((clientIfNoneMatch != null && clientIfNoneMatch.equals(eTag))
                || (clientModifiedSince != null && clientModifiedSince.equals(lastModifiedTime.toString()))) {
            // logger.info("uri = /{} 304 NOT Modified", webFilePath);
            resp.setStatus(304);
            return;
        }

        // 根据文件名，猜测 content-type
        String mime = Files.probeContentType(Path.of(filename));
        if (mime == null) {
            // 注意：probeContentType 对于一些常见的文件，反而无法返回正确的mime,
            // 真实奇怪，查了没找到具体的说法，只能先特别处理一下
            if (filename.endsWith(".css")) {
                mime = "text/css";
            } else if (filename.endsWith(".js")) {
                mime = "text/javascript";
            } else {
                mime = "application/octet-stream";
            }
        }

        resp.setContentType(mime);
        resp.setHeader("Server", "north/1.0");

        // Response file content
        OutputStream outputStream = resp.getOutputStream();
        this.getClass().getClassLoader().getResourceAsStream(webFilePath).transferTo(outputStream);

        outputStream.flush();
    }
}
