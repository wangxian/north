package top.xiqiu.north.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件服务器 for /static/*
 */
public class FileServerServlet extends HttpServlet {
    /**
     * logger
     **/
    private static Logger logger = LoggerFactory.getLogger(FileServerServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String webFilePath = req.getRequestURI();

        // 处理浏览器自动访问 /favicon.ico 返回404问题
        // 映射到 /static/favicon.ico，这是一个相对简单到做法
        if ("/favicon.ico".equals(webFilePath)) {
            webFilePath = "/static/favicon.ico";
        }

        logger.debug("GET {}", webFilePath);

        // 获取真实文件路径, 在 fatjar 下不可行
        // String filepath = req.getServletContext().getRealPath(webFilePath);
        // if (filepath == null) {
        //     resp.sendError(404);
        //     return;
        // }

        // Path path = Paths.get(filepath);
        // if (!path.toFile().isFile()) {
        //     resp.sendError(404);
        //     return;
        // }

        webFilePath = webFilePath.substring(1);
        final URL resource = this.getClass().getClassLoader().getResource(webFilePath);
        if (resource == null) {
            resp.sendError(404);
            return;
        }
        String filepath = resource.getPath();
        System.out.println("filepath=" + filepath);

        // 根据文件名，猜测 content-type
        String mime = Files.probeContentType(Path.of(filepath));
        if (mime == null) {
            // 注意：probeContentType 对于一些常见的文件，反而无法返回正确的mime,
            // 真实奇怪，查了没找到具体的说法，只能先特别处理一下
            if (filepath.endsWith(".css")) {
                mime = "text/css";
            } else if (filepath.endsWith(".js")) {
                mime = "text/javascript";
            } else {
                mime = "application/octet-stream";
            }
        }

        resp.setContentType(mime);
        resp.setHeader("Server", "north/1.0");

        // Response file content
        // OutputStream outputStream = resp.getOutputStream();
        // try (InputStream inputStream = new BufferedInputStream(new FileInputStream(filepath))) {
        //     inputStream.transferTo(outputStream);
        // }

        // Response file content
        OutputStream outputStream = resp.getOutputStream();
        this.getClass().getClassLoader().getResourceAsStream(webFilePath).transferTo(outputStream);

        outputStream.flush();
    }
}
