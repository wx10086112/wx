package com.ruoyi.web.controller.mall;

import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.product.domain.GrouponActivity;
import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.service.IGrouponActivityService;
import com.ruoyi.mall.product.service.IProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 总后台团购活动管理Controller
 */
@RestController
@RequestMapping("/mall/groupon")
public class MallGrouponController extends BaseController {

    @Resource
    private IGrouponActivityService grouponActivityService;

    @Resource
    private IProductService productService;

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
        return AjaxResult.success(grouponActivityService.selectGrouponActivityById(id));
    }

    /**
     * 新增团购活动
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:add')")
    @Log(title = "团购活动管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GrouponActivity grouponActivity) {
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
        return toAjax(grouponActivityService.updateGrouponActivity(grouponActivity));
    }

    /**
     * 删除团购活动
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:remove')")
    @Log(title = "团购活动管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(grouponActivityService.deleteGrouponActivityByIds(ids));
    }

    /**
     * 修改活动状态（上架/下架）
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:edit')")
    @Log(title = "团购活动状态修改", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult changeStatus(@RequestBody GrouponActivity grouponActivity) {
        return toAjax(grouponActivityService.updateGrouponActivity(grouponActivity));
    }

    /**
     * 获取商家的团购活动选项
     */
    @GetMapping("/options")
    public AjaxResult options(@RequestParam(required = false) Long merchantId) {
        List<GrouponActivity> list;
        if (merchantId != null) {
            list = grouponActivityService.selectByMerchantId(merchantId);
        } else {
            list = grouponActivityService.selectActiveActivities();
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
            dirBuilder.append("groupon").append(File.separator);

            if (grouponId != null) {
                dirBuilder.append(grouponId).append(File.separator);
            } else if (tempToken != null) {
                dirBuilder.append("temp").append(File.separator).append(tempToken).append(File.separator);
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

            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("fileName", fileName);
            result.put("relativePath", relativePath.replace(File.separator, "/"));
            return AjaxResult.success(result);

        } catch (IOException e) {
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取团购活动下的商品列表
     */
    @GetMapping("/product/list")
    public AjaxResult getProductList(@RequestParam Long grouponId) {
        Product query = new Product();
        query.setGrouponId(grouponId);
        List<Product> list = productService.selectProductList(query);
        return AjaxResult.success(list);
    }

    /**
     * 绑定商品到团购活动
     */
    @PostMapping("/product/bind")
    public AjaxResult bindProduct(@RequestBody Map<String, Object> params) {
        Long grouponId = Long.valueOf(params.get("grouponId").toString());
        List<Number> productIds = (List<Number>) params.get("productIds");

        GrouponActivity activity = grouponActivityService.selectGrouponActivityById(grouponId);
        if (activity == null) {
            return AjaxResult.error("团购活动不存在");
        }

        for (Number productId : productIds) {
            Product product = productService.selectProductById(productId.longValue());
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
    @PostMapping("/product/unbind")
    public AjaxResult unbindProduct(@RequestBody Map<String, Object> params) {
        List<Number> productIds = (List<Number>) params.get("productIds");

        for (Number productId : productIds) {
            Product product = productService.selectProductById(productId.longValue());
            if (product != null) {
                product.setGrouponId(null);
                productService.updateProduct(product);
            }
        }
        return AjaxResult.success();
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
