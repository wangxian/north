package top.xiqiu.north.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件服务器 for /static/*
 */
public class FileServerServlet extends HttpServlet {
    /**
     * logger
     **/
    private static Logger logger = LoggerFactory.getLogger(FileServerServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String webFilePath = req.getRequestURI();

        // 处理浏览器自动访问 /favicon.ico 返回404问题
        // 映射到 /static/favicon.ico，这是一个相对简单到做法
        if ("/favicon.ico".equals(webFilePath)) {
            webFilePath = "/static/favicon.ico";
        }

        logger.debug("access GET {}", webFilePath);

        // 获取真实文件路径
        String filepath = req.getServletContext().getRealPath(webFilePath);

        if (filepath == null) {
            resp.sendError(404);
            return;
        }

        Path path = Paths.get(filepath);
        if (!path.toFile().isFile()) {
            resp.sendError(404);
            return;
        }

        // 根据文件名，猜测 content-type
        String mime = Files.probeContentType(path);
        if (mime == null) {
            // 注意：probeContentType 对于一些常见的文件，反而无法返回正确的mime,
            // 真实奇怪，查了没找到具体的说法
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
        OutputStream outputStream = resp.getOutputStream();
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(filepath))) {
            inputStream.transferTo(outputStream);
        }

        outputStream.flush();
    }
}
