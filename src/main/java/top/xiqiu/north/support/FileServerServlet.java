package top.xiqiu.north.support;

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
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 获取真实文件路径
        String filepath = req.getServletContext().getRealPath(req.getRequestURI());

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
