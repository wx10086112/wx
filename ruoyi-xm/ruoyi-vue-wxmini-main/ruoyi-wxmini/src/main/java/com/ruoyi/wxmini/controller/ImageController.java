package com.ruoyi.wxmini.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * 图片资源代理：文件不存在时返回占位图，避免500错误
 */
@RestController
public class ImageController {

    @Value("${ruoyi.profile}")
    private String profilePath;

    private static final byte[] PLACEHOLDER_SVG = (
            "<svg xmlns='http://www.w3.org/2000/svg' width='200' height='200' viewBox='0 0 200 200'>"
                    + "<rect width='200' height='200' fill='#f0f0f0'/>"
                    + "<text x='100' y='105' text-anchor='middle' font-size='14' fill='#999'>暂无图片</text>"
                    + "</svg>"
    ).getBytes(java.nio.charset.StandardCharsets.UTF_8);

    @GetMapping("/profile/**")
    public void serveImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getRequestURI(); // e.g. /profile/upload/product/xxx.jpg
        String relativePath = path.substring("/profile/".length());
        File file = new File(profilePath, relativePath);

        if (file.exists() && file.isFile()) {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            response.setContentType(contentType);
            response.setContentLengthLong(file.length());
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = fis.read(buf)) != -1) {
                    os.write(buf, 0, len);
                }
            }
        } else {
            response.setContentType("image/svg+xml");
            response.setContentLength(PLACEHOLDER_SVG.length);
            response.getOutputStream().write(PLACEHOLDER_SVG);
        }
    }
}
