package com.ruoyi.web.controller.mall;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.domain.ProductImage;
import com.ruoyi.mall.product.service.IProductImageService;
import com.ruoyi.mall.product.service.IProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 后台管理端商品图片上传Controller
 */
@RestController
@RequestMapping("/merchant/product/image")
public class MallMerchantProductImageController extends BaseController {

    @Resource
    private IProductImageService productImageService;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private IProductService productService;

    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "main", "detail", "avatar", "banner", "cover", "poster", "sku"
    ));

    /**
     * 商品图片上传接口
     */
    @PreAuthorize("@ss.hasPermi('mall:merchant:edit')")
    @PostMapping("/upload")
    public AjaxResult upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("merchantId") Long merchantId,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "imageType", required = false, defaultValue = "main") String imageType) {

        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }

        // 归属校验：当前用户只能给自己名下商家上传
        Merchant merchant = merchantService.selectMerchantById(merchantId);
        if (merchant == null) {
            return AjaxResult.error("商家不存在");
        }
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null && !effMerchantId.equals(merchantId)) {
            return AjaxResult.error("无权限操作该商家");
        }
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !effDistributorId.equals(merchant.getDistributorId())) {
            return AjaxResult.error("无权限操作该商家");
        }

        String normalizedImageType = imageType == null ? "main" : imageType.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_TYPES.contains(normalizedImageType)) {
            return AjaxResult.error("不支持的图片类型");
        }

        if (productId != null) {
            Product product = productService.selectProductById(productId);
            if (product == null || product.getMerchantId() == null || !merchantId.equals(product.getMerchantId())) {
                return AjaxResult.error("商品不存在或不属于当前商户");
            }
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return AjaxResult.error("文件大小不能超过5MB");
        }

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

        String uploadRoot = RuoYiConfig.getProfile() + "/merchant_images";

        try {
            StringBuilder dirBuilder = new StringBuilder();
            dirBuilder.append(merchantId).append("/");
            dirBuilder.append("product").append("/");

            if (productId != null) {
                dirBuilder.append(productId).append("/");
            } else {
                dirBuilder.append("temp").append("/");
            }

            String subDir = dirBuilder.toString();
            Path basePath = Paths.get(uploadRoot).toAbsolutePath().normalize();
            Path dirPath = basePath.resolve(subDir).normalize();
            if (!dirPath.startsWith(basePath)) {
                return AjaxResult.error("非法上传路径");
            }
            Files.createDirectories(dirPath);

            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String fileName = normalizedImageType + "_" + uuid + "." + ext;
            String relativePath = subDir + fileName;

            Path destPath = basePath.resolve(relativePath).normalize();
            if (!destPath.startsWith(basePath)) {
                return AjaxResult.error("非法上传路径");
            }
            file.transferTo(destPath.toFile());

            String url = "/profile/merchant_images/" + relativePath;

            if (productId != null) {
                ProductImage image = new ProductImage();
                image.setProductId(productId);
                image.setMerchantId(merchantId);
                image.setImageType(normalizedImageType);
                image.setImageUrl(url);
                image.setStatus(1);
                productImageService.insertProductImage(image);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("fileName", fileName);
            result.put("relativePath", relativePath);
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
