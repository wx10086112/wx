package com.ruoyi.web.controller.mall;

import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.product.domain.GrouponActivity;
import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.service.IGrouponActivityService;
import com.ruoyi.mall.product.service.IProductService;
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

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 总后台团购活动管理
 */
@RestController
@RequestMapping("/mall/groupon")
public class MallGrouponController extends BaseController {

    @Resource
    private IGrouponActivityService grouponActivityService;

    @Resource
    private IProductService productService;

    @Resource
    private IMerchantService merchantService;

    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "main", "detail", "avatar", "banner", "cover", "poster", "sku"
    ));

    /**
     * 查询团购活动列表
     */
    @DataScopeBiz(merchantAlias = "groupon_activity")
    @PreAuthorize("@ss.hasPermi('mall:groupon:list')")
    @GetMapping("/list")
    public TableDataInfo list(GrouponActivity grouponActivity) {
        startPage();
        List<GrouponActivity> list = grouponActivityService.selectGrouponActivityList(grouponActivity);
        return getDataTable(list);
    }

    /**
     * 获取团购活动详细信息
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        GrouponActivity activity = grouponActivityService.selectGrouponActivityById(id);
        AjaxResult denied = checkActivityAccess(activity, "团购活动");
        if (denied != null) {
            return denied;
        }
        return AjaxResult.success(activity);
    }

    /**
     * 新增团购活动
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:add')")
    @Log(title = "团购活动管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GrouponActivity grouponActivity) {
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null) {
            grouponActivity.setMerchantId(effMerchantId);
        }
        AjaxResult denied = checkMerchantAccess(grouponActivity.getMerchantId(), "团购活动");
        if (denied != null) {
            return denied;
        }
        AjaxResult operateCheck = checkMerchantCanOperate(grouponActivity.getMerchantId(), grouponActivity.getStatus());
        if (operateCheck != null) {
            return operateCheck;
        }
        grouponActivity.setSourceType("ADMIN");
        return toAjax(grouponActivityService.insertGrouponActivity(grouponActivity));
    }

    /**
     * 修改团购活动
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:edit')")
    @Log(title = "团购活动管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GrouponActivity grouponActivity) {
        GrouponActivity existing = grouponActivityService.selectGrouponActivityById(grouponActivity.getId());
        AjaxResult denied = checkActivityAccess(existing, "团购活动");
        if (denied != null) {
            return denied;
        }
        AjaxResult operateCheck = checkMerchantCanOperate(existing.getMerchantId(), grouponActivity.getStatus());
        if (operateCheck != null) {
            return operateCheck;
        }
        if (MallDataScopeHelper.currentEffectiveMerchantId() != null
                || MallDataScopeHelper.currentEffectiveDistributorId() != null) {
            grouponActivity.setMerchantId(existing.getMerchantId());
        }
        return toAjax(grouponActivityService.updateGrouponActivity(grouponActivity));
    }

    /**
     * 删除团购活动
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:remove')")
    @Log(title = "团购活动管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        for (Long id : ids) {
            GrouponActivity activity = grouponActivityService.selectGrouponActivityById(id);
            AjaxResult denied = checkActivityAccess(activity, "团购活动");
            if (denied != null) {
                return denied;
            }
        }
        return toAjax(grouponActivityService.deleteGrouponActivityByIds(ids));
    }

    /**
     * 修改活动状态
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:edit')")
    @Log(title = "团购活动状态修改", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult changeStatus(@RequestBody GrouponActivity grouponActivity) {
        GrouponActivity existing = grouponActivityService.selectGrouponActivityById(grouponActivity.getId());
        AjaxResult denied = checkActivityAccess(existing, "团购活动");
        if (denied != null) {
            return denied;
        }
        AjaxResult operateCheck = checkMerchantCanOperate(existing.getMerchantId(), grouponActivity.getStatus());
        if (operateCheck != null) {
            return operateCheck;
        }
        GrouponActivity update = new GrouponActivity();
        update.setId(grouponActivity.getId());
        update.setStatus(grouponActivity.getStatus());
        return toAjax(grouponActivityService.updateGrouponActivity(update));
    }

    /**
     * 获取商家的团购活动选项
     */
    @GetMapping("/options")
    @PreAuthorize("@ss.hasPermi('mall:groupon:list')")
    public AjaxResult options(@RequestParam(required = false) Long merchantId) {
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null) {
            merchantId = effMerchantId;
        }

        List<GrouponActivity> list;
        if (merchantId != null) {
            AjaxResult denied = checkMerchantAccess(merchantId, "团购活动");
            if (denied != null) {
                return denied;
            }
            list = grouponActivityService.selectByMerchantId(merchantId);
        } else {
            list = grouponActivityService.selectActiveActivities();
            list.removeIf(activity -> !isMerchantAccessible(activity.getMerchantId()));
        }

        List<Map<String, Object>> options = new ArrayList<>();
        for (GrouponActivity activity : list) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", activity.getId());
            option.put("name", activity.getName());
            options.add(option);
        }
        return AjaxResult.success(options);
    }

    /**
     * 团购活动图片上传
     */
    @PostMapping("/image/upload")
    public AjaxResult uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("merchantId") Long merchantId,
            @RequestParam(value = "grouponId", required = false) Long grouponId,
            @RequestParam(value = "imageType", required = false, defaultValue = "cover") String imageType,
            @RequestParam(value = "tempToken", required = false) String tempToken) {

        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }

        String normalizedImageType = imageType == null ? "cover" : imageType.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_TYPES.contains(normalizedImageType)) {
            return AjaxResult.error("不支持的图片类型");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return AjaxResult.error("文件大小不能超过5MB");
        }

        AjaxResult denied = checkMerchantAccess(merchantId, "团购活动图片");
        if (denied != null) {
            return denied;
        }
        if (grouponId != null) {
            GrouponActivity activity = grouponActivityService.selectGrouponActivityById(grouponId);
            denied = checkActivityAccess(activity, "团购活动图片");
            if (denied != null) {
                return denied;
            }
            if (!merchantId.equals(activity.getMerchantId())) {
                return AjaxResult.error("团购活动与商户不匹配");
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
            dirBuilder.append("groupon").append("/");

            if (grouponId != null) {
                dirBuilder.append(grouponId).append("/");
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

    /**
     * 获取团购活动下的商品列表
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:list')")
    @GetMapping("/product/list")
    public AjaxResult getProductList(@RequestParam Long grouponId) {
        GrouponActivity activity = grouponActivityService.selectGrouponActivityById(grouponId);
        AjaxResult denied = checkActivityAccess(activity, "团购活动");
        if (denied != null) {
            return denied;
        }

        Product query = new Product();
        query.setGrouponId(grouponId);
        List<Product> list = productService.selectProductList(query);
        return AjaxResult.success(list);
    }

    /**
     * 绑定商品到团购活动
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:edit')")
    @PostMapping("/product/bind")
    public AjaxResult bindProduct(@RequestBody Map<String, Object> params) {
        Long grouponId = Long.valueOf(params.get("grouponId").toString());
        List<Number> productIds = (List<Number>) params.get("productIds");

        GrouponActivity activity = grouponActivityService.selectGrouponActivityById(grouponId);
        AjaxResult denied = checkActivityAccess(activity, "团购活动");
        if (denied != null) {
            return denied;
        }

        if (productIds == null) {
            return AjaxResult.success();
        }
        for (Number productId : productIds) {
            Product product = productService.selectProductById(productId.longValue());
            denied = checkProductAccess(product, "商品");
            if (denied != null) {
                return denied;
            }
            if (product != null && product.getMerchantId().equals(activity.getMerchantId())) {
                product.setGrouponId(grouponId);
                productService.updateProduct(product);
            }
        }
        return AjaxResult.success();
    }

    /**
     * 解绑商品
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:edit')")
    @PostMapping("/product/unbind")
    public AjaxResult unbindProduct(@RequestBody Map<String, Object> params) {
        List<Number> productIds = (List<Number>) params.get("productIds");
        if (productIds == null) {
            return AjaxResult.success();
        }

        for (Number productId : productIds) {
            Product product = productService.selectProductById(productId.longValue());
            AjaxResult denied = checkProductAccess(product, "商品");
            if (denied != null) {
                return denied;
            }
            if (product != null) {
                product.setGrouponId(null);
                productService.updateProduct(product);
            }
        }
        return AjaxResult.success();
    }

    private AjaxResult checkActivityAccess(GrouponActivity activity, String label) {
        if (activity == null) {
            return AjaxResult.error(label + "不存在");
        }
        return checkMerchantAccess(activity.getMerchantId(), label);
    }

    private AjaxResult checkProductAccess(Product product, String label) {
        if (product == null) {
            return AjaxResult.error(label + "不存在");
        }
        return checkMerchantAccess(product.getMerchantId(), label);
    }

    private AjaxResult checkMerchantAccess(Long merchantId, String label) {
        if (merchantId == null) {
            return AjaxResult.error(label + "商户不能为空");
        }
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null && !effMerchantId.equals(merchantId)) {
            return AjaxResult.error("无权操作该" + label);
        }
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null) {
            Merchant merchant = merchantService.selectMerchantById(merchantId);
            if (merchant == null || !effDistributorId.equals(merchant.getDistributorId())) {
                return AjaxResult.error("无权操作该" + label);
            }
        }
        return null;
    }

    private boolean isMerchantAccessible(Long merchantId) {
        return checkMerchantAccess(merchantId, "团购活动") == null;
    }

    private AjaxResult checkMerchantCanOperate(Long merchantId, Integer status) {
        if (status == null || status != 1 || merchantId == null) {
            return null;
        }
        Merchant merchant = merchantService.selectMerchantById(merchantId);
        if (merchant != null && !merchant.canOperate()) {
            return AjaxResult.error("商家运营条件不满足：" + merchant.getOperateBlockReason());
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
