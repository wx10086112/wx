package com.ruoyi.web.controller.mall;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.product.domain.ProductImage;
import com.ruoyi.mall.product.service.IProductImageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 后台管理端商品图片上传Controller
 */
@RestController
@RequestMapping("/merchant/product/image")
public class MallMerchantProductImageController extends BaseController {

    @Resource
    private IProductImageService productImageService;

    /**
     * 上传根目录：E:\ruoyi\ruoyi-xm\merchant_images
     */
    private static final String UPLOAD_ROOT = "E:\\ruoyi\\ruoyi-xm\\merchant_images";

    /**
     * 商品图片上传接口
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

        // 校验文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return AjaxResult.error("文件大小不能超过5MB");
        }

        // 校验文件类型
        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);
        if (!isImageExtension(ext)) {
            return AjaxResult.error("仅支持jpg/jpeg/png/webp格式");
        }

        try {
            // 构建目录: merchant_images/{merchantId}/product/{productId}/
            StringBuilder dirBuilder = new StringBuilder();
            dirBuilder.append(merchantId).append(File.separator);
            dirBuilder.append("product").append(File.separator);

            if (productId != null) {
                dirBuilder.append(productId).append(File.separator);
            } else {
                dirBuilder.append("temp").append(File.separator);
            }

            String subDir = dirBuilder.toString();
            File dir = new File(UPLOAD_ROOT, subDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成文件名: {imageType}_{uuid8}.{ext}
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String fileName = imageType + "_" + uuid + "." + ext;
            String relativePath = subDir + fileName;

            // 保存文件
            File dest = new File(UPLOAD_ROOT, relativePath);
            file.transferTo(dest);

            // 生成访问URL
            String url = "/profile/merchant_images/" + relativePath.replace(File.separator, "/");

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
            result.put("relativePath", relativePath.replace(File.separator, "/"));
            return AjaxResult.success(result);

        } catch (IOException e) {
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        }
    }

    private boolean isImageExtension(String ext) {
        return "jpg".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext)
            || "png".equalsIgnoreCase(ext) || "webp".equalsIgnoreCase(ext);
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
