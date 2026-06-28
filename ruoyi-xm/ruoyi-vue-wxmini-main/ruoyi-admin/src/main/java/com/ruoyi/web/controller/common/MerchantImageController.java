package com.ruoyi.web.controller.common;

import com.ruoyi.common.config.RuoYiConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Serves merchant images and generates small list thumbnails on demand.
 */
@RestController
public class MerchantImageController {
    private static final String MERCHANT_IMAGES_PREFIX = "/profile/merchant_images/";
    private static final String MERCHANT_GOODS_PREFIX = "/profile/merchant-goods/";
    private static final String THUMB_ROOT = "_thumb";
    private static final String LIST_THUMB = "list";
    private static final String DETAIL_THUMB = "detail";
    private static final int LIST_THUMB_SIZE = 360;
    private static final int DETAIL_THUMB_SIZE = 720;

    @RequestMapping(value = { MERCHANT_IMAGES_PREFIX + "**", MERCHANT_GOODS_PREFIX + "**" },
            method = { RequestMethod.GET, RequestMethod.HEAD })
    public ResponseEntity<Resource> getMerchantImage(
            HttpServletRequest request,
            @RequestParam(value = "thumb", required = false) String thumb) throws IOException {
        String urlPrefix = matchUrlPrefix(request);
        if (urlPrefix == null) {
            return ResponseEntity.notFound().build();
        }
        Path basePath = resolveBasePath(urlPrefix);
        String relativePath = extractRelativePath(request, urlPrefix);
        Path imagePath = resolveImagePath(basePath, relativePath);
        if (imagePath == null || !Files.isRegularFile(imagePath)) {
            return ResponseEntity.notFound().build();
        }

        Path responsePath = imagePath;
        if (LIST_THUMB.equalsIgnoreCase(String.valueOf(thumb)) || DETAIL_THUMB.equalsIgnoreCase(String.valueOf(thumb))) {
            int thumbSize = DETAIL_THUMB.equalsIgnoreCase(String.valueOf(thumb)) ? DETAIL_THUMB_SIZE : LIST_THUMB_SIZE;
            Path thumbPath = ensureThumbnail(basePath, relativePath, imagePath, String.valueOf(thumb).toLowerCase(Locale.ROOT), thumbSize);
            if (thumbPath != null && Files.isRegularFile(thumbPath)) {
                responsePath = thumbPath;
            }
        }

        return buildFileResponse(responsePath);
    }

    private String matchUrlPrefix(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        if (uri.contains(MERCHANT_IMAGES_PREFIX)) {
            return MERCHANT_IMAGES_PREFIX;
        }
        if (uri.contains(MERCHANT_GOODS_PREFIX)) {
            return MERCHANT_GOODS_PREFIX;
        }
        return null;
    }

    private Path resolveBasePath(String urlPrefix) {
        if (MERCHANT_GOODS_PREFIX.equals(urlPrefix)) {
            return Paths.get(RuoYiConfig.getProfile(), "merchant-goods").toAbsolutePath().normalize();
        }
        return Paths.get(RuoYiConfig.getProfile(), "merchant_images").toAbsolutePath().normalize();
    }

    private String extractRelativePath(HttpServletRequest request, String urlPrefix) throws IOException {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        int index = uri.indexOf(urlPrefix);
        String relativePath = index >= 0 ? uri.substring(index + urlPrefix.length()) : "";
        return URLDecoder.decode(relativePath, StandardCharsets.UTF_8.name()).replace('\\', '/');
    }

    private Path resolveImagePath(Path basePath, String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }
        Path imagePath = basePath.resolve(relativePath).normalize();
        if (!imagePath.startsWith(basePath)) {
            return null;
        }
        return imagePath;
    }

    private Path ensureThumbnail(Path basePath, String relativePath, Path imagePath, String thumbName, int thumbSize) throws IOException {
        Path thumbPath = basePath.resolve(THUMB_ROOT).resolve(thumbName).resolve(replaceExtension(relativePath, "jpg")).normalize();
        if (!thumbPath.startsWith(basePath.resolve(THUMB_ROOT).normalize())) {
            return null;
        }
        if (Files.isRegularFile(thumbPath)
                && Files.getLastModifiedTime(thumbPath).toMillis() >= Files.getLastModifiedTime(imagePath).toMillis()) {
            return thumbPath;
        }

        BufferedImage source = ImageIO.read(imagePath.toFile());
        if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0) {
            return null;
        }

        int cropSize = Math.min(source.getWidth(), source.getHeight());
        int sourceX = (source.getWidth() - cropSize) / 2;
        int sourceY = (source.getHeight() - cropSize) / 2;
        int targetSize = Math.min(thumbSize, cropSize);

        BufferedImage target = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(source, 0, 0, targetSize, targetSize,
                    sourceX, sourceY, sourceX + cropSize, sourceY + cropSize, null);
        } finally {
            graphics.dispose();
        }

        Files.createDirectories(thumbPath.getParent());
        Path tempPath = thumbPath.resolveSibling(thumbPath.getFileName() + ".tmp");
        ImageIO.write(target, "jpg", tempPath.toFile());
        Files.move(tempPath, thumbPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return thumbPath;
    }

    private String replaceExtension(String relativePath, String extension) {
        int slashIndex = relativePath.lastIndexOf('/');
        int dotIndex = relativePath.lastIndexOf('.');
        if (dotIndex > slashIndex) {
            return relativePath.substring(0, dotIndex + 1) + extension;
        }
        return relativePath + "." + extension;
    }

    private ResponseEntity<Resource> buildFileResponse(Path path) throws IOException {
        MediaType mediaType = detectMediaType(path);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(Files.size(path))
                .lastModified(Files.getLastModifiedTime(path).toMillis())
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(new FileSystemResource(path));
    }

    private MediaType detectMediaType(Path path) throws IOException {
        String type = Files.probeContentType(path);
        if (type != null) {
            return MediaType.parseMediaType(type);
        }
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (fileName.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }
}
