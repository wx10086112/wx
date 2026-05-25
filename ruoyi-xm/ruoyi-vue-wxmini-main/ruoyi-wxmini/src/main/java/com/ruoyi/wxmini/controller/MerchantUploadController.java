package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.product.domain.ProductImage;
import com.ruoyi.mall.product.service.IProductImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/wxmini/merchant")
public class MerchantUploadController {

    @Value("${ruoyi.profile}")
    private String profilePath;

    @Resource
    private IProductImageService productImageService;

    /**
     * 通用图片上传接口
     * 支持按商家ID/商品ID/图片类型自动分类存储
     */
    @PostMapping("/upload")
    public AjaxResult upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("merchantId") Long merchantId,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "imageType", required = false, defaultValue = "main") String imageType) {

        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }

        // 校验文件大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return AjaxResult.error("文件大小不能超过10MB");
        }

        // 校验文件类型
        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);
        if (!isImageExtension(ext)) {
            return AjaxResult.error("仅支持jpg/png/gif格式");
        }

        try {
            // 构建目录: merchant/{merchantId}/{type}/ 或 merchant/{merchantId}/product/{productId}/{type}/
            StringBuilder dirBuilder = new StringBuilder("merchant/");
            dirBuilder.append(merchantId).append("/");

            if (productId != null) {
                dirBuilder.append("product/").append(productId).append("/");
            }
            dirBuilder.append(imageType).append("/");

            String subDir = dirBuilder.toString();
            File dir = new File(profilePath, subDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成文件名: {type}_{uuid8}.jpg
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String fileName = imageType + "_" + uuid + "." + ext;
            String relativePath = subDir + fileName;

            // 保存文件
            File dest = new File(profilePath, relativePath);
            file.transferTo(dest);

            String url = "/profile/" + relativePath;

            // 如果传了productId，写入product_image表
            if (productId != null) {
                ProductImage image = new ProductImage();
                image.setProductId(productId);
                image.setMerchantId(merchantId);
                image.setImageType(imageType);
                image.setImageUrl(url);
                image.setStatus(1);
                productImageService.insertProductImage(image);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("fileName", fileName);
            result.put("imageType", imageType);
            return AjaxResult.success(result);

        } catch (IOException e) {
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        }
    }

    private boolean isImageExtension(String ext) {
        return "jpg".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext)
            || "png".equalsIgnoreCase(ext) || "gif".equalsIgnoreCase(ext);
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
