package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }
        try {
            String subDir = "wxmini";
            String uploadDir = profilePath + "/" + subDir + "/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalName = file.getOriginalFilename();
            String ext = "jpg";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf(".") + 1);
            }
            String fileName = subDir + "/" + System.currentTimeMillis() + "_"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "." + ext;
            File dest = new File(profilePath + "/" + fileName);
            file.transferTo(dest);

            Map<String, Object> result = new HashMap<>();
            result.put("url", "/profile/" + fileName);
            result.put("fileName", originalName);
            return AjaxResult.success(result);
        } catch (IOException e) {
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        }
    }
}
