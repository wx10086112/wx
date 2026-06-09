package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/wxmini/common")
public class WxCommonController {

    @Value("${ruoyi.profile}")
    private String profilePath;

    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) {
        String currentUserId = WxMiniUserContext.getCurrentUserId();
        if (StringUtils.isBlank(currentUserId)) {
            return AjaxResult.error("请先登录");
        }
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return AjaxResult.error("文件大小不能超过10MB");
        }

        // 根据 magic byte 检测真实类型
        String ext;
        try {
            ext = detectImageExtension(file.getInputStream());
        } catch (IOException e) {
            return AjaxResult.error("文件读取失败");
        }
        if (ext == null) {
            return AjaxResult.error("仅支持jpg/png/webp格式");
        }

        String contentType = file.getContentType();
        if (contentType != null && !isAllowedMimeType(contentType)) {
            return AjaxResult.error("仅支持jpg/png/webp格式");
        }

        try {
            Long merchantId = WxMiniUserContext.getCurrentMerchantId();
            if (merchantId == null) {
                merchantId = WxMiniUserContext.getAppIdMerchantId();
            }
            String safeUserId = currentUserId.replaceAll("[^a-zA-Z0-9_-]", "_");
            String subDir = "wxmini/" + (merchantId != null ? merchantId : "unknown") + "/user/" + safeUserId;
            Path basePath = Paths.get(profilePath).toAbsolutePath().normalize();
            Path dirPath = basePath.resolve(subDir).normalize();
            if (!dirPath.startsWith(basePath)) {
                return AjaxResult.error("非法上传路径");
            }
            Files.createDirectories(dirPath);

            // 后端生成文件名，不使用用户原始文件名
            String fileName = subDir + "/" + System.currentTimeMillis() + "_"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "." + ext;
            Path destPath = basePath.resolve(fileName).normalize();
            if (!destPath.startsWith(basePath)) {
                return AjaxResult.error("非法上传路径");
            }
            file.transferTo(destPath.toFile());

            Map<String, Object> result = new HashMap<>();
            result.put("url", "/profile/" + fileName);
            result.put("fileName", fileName);
            return AjaxResult.success(result);
        } catch (IOException e) {
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        }
    }

    private boolean isAllowedMimeType(String contentType) {
        return "image/jpeg".equals(contentType)
                || "image/png".equals(contentType)
                || "image/webp".equals(contentType);
    }

    private String detectImageExtension(InputStream is) throws IOException {
        byte[] header = new byte[12];
        int read = is.read(header);
        if (read < 4) {
            return null;
        }
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return "jpg";
        }
        if (header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
            return "png";
        }
        if (read >= 12 && header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46
                && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) {
            return "webp";
        }
        return null;
    }
}
