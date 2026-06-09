package com.ruoyi.mall.product.controller;

import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/mall/product")
public class MallProductController extends BaseController {

    @Autowired
    private IProductService productService;

    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "main", "detail", "cover", "sku"
    ));

    @DataScopeBiz(merchantAlias = "product")
    @PreAuthorize("@ss.hasPermi('mall:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(Product product) {
        startPage();
        List<Product> list = productService.selectProductList(product);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:product:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        Product product = productService.selectProductById(id);
        AjaxResult denied = checkProductAccess(product, "商品");
        if (denied != null) {
            return denied;
        }
        return AjaxResult.success(product);
    }

    @PreAuthorize("@ss.hasPermi('mall:product:add')")
    @Log(title = "商品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Product product) {
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null) {
            product.setMerchantId(effMerchantId);
        }
        AjaxResult denied = checkMerchantAccess(product.getMerchantId(), "商品");
        if (denied != null) {
            return denied;
        }
        return toAjax(productService.insertProduct(product));
    }

    @PreAuthorize("@ss.hasPermi('mall:product:edit')")
    @Log(title = "商品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Product product) {
        Product existing = productService.selectProductById(product.getId());
        AjaxResult denied = checkProductAccess(existing, "商品");
        if (denied != null) {
            return denied;
        }
        if (MallDataScopeHelper.currentEffectiveMerchantId() != null
                || MallDataScopeHelper.currentEffectiveDistributorId() != null) {
            product.setMerchantId(existing.getMerchantId());
        }
        return toAjax(productService.updateProduct(product));
    }

    @PreAuthorize("@ss.hasPermi('mall:product:remove')")
    @Log(title = "商品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        for (Long id : ids) {
            Product product = productService.selectProductById(id);
            AjaxResult denied = checkProductAccess(product, "商品");
            if (denied != null) {
                return denied;
            }
        }
        return toAjax(productService.deleteProductByIds(ids));
    }

    /**
     * 普通商品图片上传
     */
    @PreAuthorize("@ss.hasPermi('mall:product:edit')")
    @PostMapping("/image/upload")
    public AjaxResult uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("merchantId") Long merchantId,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "imageType", required = false, defaultValue = "main") String imageType,
            @RequestParam(value = "tempToken", required = false) String tempToken) {

        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }
        String normalizedImageType = imageType == null ? "main" : imageType.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_TYPES.contains(normalizedImageType)) {
            return AjaxResult.error("不支持的图片类型");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return AjaxResult.error("文件大小不能超过5MB");
        }
        AjaxResult denied = checkMerchantAccess(merchantId, "商品图片");
        if (denied != null) {
            return denied;
        }
        if (productId != null) {
            Product product = productService.selectProductById(productId);
            denied = checkProductAccess(product, "商品图片");
            if (denied != null) {
                return denied;
            }
            if (!merchantId.equals(product.getMerchantId())) {
                return AjaxResult.error("商品与商户不匹配");
            }
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
            } else if (tempToken != null) {
                if (!tempToken.matches("^[a-zA-Z0-9_-]{1,64}$")) {
                    return AjaxResult.error("无效的tempToken");
                }
                dirBuilder.append("temp").append("/").append(tempToken).append("/");
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

            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("fileName", fileName);
            result.put("relativePath", relativePath);
            return AjaxResult.success(result);

        } catch (IOException e) {
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        }
    }

    private AjaxResult checkProductAccess(Product product, String label) {
        if (product == null) {
            return AjaxResult.error(label + "不存在");
        }
        return checkMerchantAccess(product.getMerchantId(), label);
    }

    private AjaxResult checkMerchantAccess(Long merchantId, String label) {
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null && !effMerchantId.equals(merchantId)) {
            return AjaxResult.error("无权操作该" + label);
        }
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !productService.isMerchantAccessibleByDistributor(merchantId, effDistributorId)) {
            return AjaxResult.error("无权操作该" + label);
        }
        return null;
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
