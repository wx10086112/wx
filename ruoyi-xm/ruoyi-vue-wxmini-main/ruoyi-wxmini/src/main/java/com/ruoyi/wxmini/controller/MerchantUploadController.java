package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.domain.ProductImage;
import com.ruoyi.mall.product.mapper.ProductMapper;
import com.ruoyi.mall.product.service.IProductImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/wxmini/merchant")
public class MerchantUploadController {

    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "main", "detail", "avatar", "banner", "cover", "poster", "sku"
    ));

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Value("${ruoyi.profile}")
    private String profilePath;

    @Resource
    private IProductImageService productImageService;
    @Resource
    private ProductMapper productMapper;

    @PostMapping("/upload")
    public AjaxResult upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("merchantId") Long merchantId,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "imageType", required = false, defaultValue = "main") String imageType) {

        Long currentMerchantId = WxMiniUserContext.getCurrentMerchantId();
        if (!WxMiniUserContext.isMerchantStaff() || currentMerchantId == null) {
            return AjaxResult.error("无商家上传权限");
        }

        if (merchantId == null || !currentMerchantId.equals(merchantId)) {
            return AjaxResult.error("上传商户与当前登录态不匹配");
        }

        String normalizedImageType = normalizeImageType(imageType);
        if (!ALLOWED_IMAGE_TYPES.contains(normalizedImageType)) {
            return AjaxResult.error("不支持的图片类型");
        }

        if (productId != null) {
            Product product = productMapper.selectProductById(productId);
            if (product == null || product.getMerchantId() == null || !currentMerchantId.equals(product.getMerchantId())) {
                return AjaxResult.error("商品不存在或不属于当前商户");
            }
        }

        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return AjaxResult.error("文件大小不能超过10MB");
        }

        // 校验真实文件类型（magic byte）
        String ext;
        try {
            ext = detectImageExtension(file.getInputStream());
        } catch (IOException e) {
            return AjaxResult.error("文件读取失败");
        }
        if (ext == null) {
            return AjaxResult.error("仅支持jpg/png/webp格式");
        }

        // MIME 类型二次校验
        String contentType = file.getContentType();
        if (contentType != null && !isAllowedMimeType(contentType)) {
            return AjaxResult.error("仅支持jpg/png/webp格式");
        }

        try {
            StringBuilder dirBuilder = new StringBuilder("merchant/");
            dirBuilder.append(currentMerchantId).append("/");
            if (productId != null) {
                dirBuilder.append("product/").append(productId).append("/");
            }
            dirBuilder.append(normalizedImageType).append("/");

            String subDir = dirBuilder.toString();
            Path basePath = Paths.get(profilePath).toAbsolutePath().normalize();
            Path dirPath = basePath.resolve(subDir).normalize();
            if (!dirPath.startsWith(basePath)) {
                return AjaxResult.error("非法上传路径");
            }
            Files.createDirectories(dirPath);

            // 后端生成文件名，不使用用户原始文件名
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String fileName = normalizedImageType + "_" + uuid + "." + ext;
            String relativePath = subDir + fileName;

            Path destPath = basePath.resolve(relativePath).normalize();
            if (!destPath.startsWith(basePath)) {
                return AjaxResult.error("非法上传路径");
            }
            file.transferTo(destPath.toFile());

            String url = "/profile/" + relativePath;

            if (productId != null) {
                ProductImage image = new ProductImage();
                image.setProductId(productId);
                image.setMerchantId(currentMerchantId);
                image.setImageType(normalizedImageType);
                image.setImageUrl(url);
                image.setStatus(1);
                productImageService.insertProductImage(image);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("fileName", fileName);
            result.put("imageType", normalizedImageType);
            return AjaxResult.success(result);

        } catch (IOException e) {
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 根据 magic byte 检测图片真实类型，返回扩展名。非图片返回 null。
     */
    private String detectImageExtension(InputStream is) throws IOException {
        byte[] header = new byte[12];
        int read = is.read(header);
        if (read < 4) {
            return null;
        }
        // JPEG: FF D8 FF
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return "jpg";
        }
        // PNG: 89 50 4E 47
        if (header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
            return "png";
        }
        // WebP: RIFF....WEBP
        if (read >= 12 && header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46
                && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) {
            return "webp";
        }
        return null;
    }

    private String normalizeImageType(String imageType) {
        return imageType == null ? "main" : imageType.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedMimeType(String contentType) {
        return "image/jpeg".equals(contentType)
                || "image/png".equals(contentType)
                || "image/webp".equals(contentType);
    }
}
