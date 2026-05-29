package com.ruoyi.web.controller.mall;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.product.domain.ProductImage;
import com.ruoyi.mall.product.service.IProductImageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !effDistributorId.equals(merchant.getDistributorId())) {
            return AjaxResult.error("无权限操作该商家");
        }

        if (!ALLOWED_IMAGE_TYPES.contains(imageType)) {
            return AjaxResult.error("不支持的图片类型");
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

        String uploadRoot = RuoYiConfig.getProfile() + "/merchant_images";

        try {
            StringBuilder dirBuilder = new StringBuilder();
            dirBuilder.append(merchantId).append(File.separator);
            dirBuilder.append("product").append(File.separator);

            if (productId != null) {
                dirBuilder.append(productId).append(File.separator);
            } else {
                dirBuilder.append("temp").append(File.separator);
            }

            String subDir = dirBuilder.toString();
            File dir = new File(uploadRoot, subDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String fileName = imageType + "_" + uuid + "." + ext;
            String relativePath = subDir + fileName;

            File dest = new File(uploadRoot, relativePath);
            file.transferTo(dest);

            String url = "/profile/merchant_images/" + relativePath.replace(File.separator, "/");

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
