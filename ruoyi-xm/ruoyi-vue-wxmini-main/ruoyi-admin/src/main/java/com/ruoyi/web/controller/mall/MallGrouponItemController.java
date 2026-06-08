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
import com.ruoyi.mall.product.domain.GrouponActivityItem;
import com.ruoyi.mall.product.service.IGrouponActivityItemService;
import com.ruoyi.mall.product.service.IGrouponActivityService;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 团购商品明细管理
 */
@RestController
@RequestMapping("/mall/groupon/item")
public class MallGrouponItemController extends BaseController {

    @Resource
    private IGrouponActivityItemService grouponActivityItemService;

    @Resource
    private IGrouponActivityService grouponActivityService;

    @Resource
    private IMerchantService merchantService;

    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "main", "detail", "avatar", "banner", "cover", "poster", "sku"
    ));

    /**
     * 查询团购商品列表
     */
    @DataScopeBiz(merchantAlias = "groupon_activity_item")
    @PreAuthorize("@ss.hasPermi('mall:groupon:list')")
    @GetMapping("/list")
    public TableDataInfo list(GrouponActivityItem query) {
        startPage();
        List<GrouponActivityItem> list = grouponActivityItemService.selectGrouponActivityItemList(query);
        return getDataTable(list);
    }

    /**
     * 根据团购活动ID查询商品列表
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:list')")
    @GetMapping("/listByGroupon")
    public AjaxResult listByGroupon(@RequestParam Long grouponId) {
        GrouponActivity activity = grouponActivityService.selectGrouponActivityById(grouponId);
        AjaxResult denied = checkActivityAccess(activity, "团购活动");
        if (denied != null) {
            return denied;
        }
        List<GrouponActivityItem> list = grouponActivityItemService.selectByGrouponId(grouponId);
        return AjaxResult.success(list);
    }

    /**
     * 获取团购商品详细信息
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        GrouponActivityItem item = grouponActivityItemService.selectGrouponActivityItemById(id);
        AjaxResult denied = checkItemAccess(item, "团购商品");
        if (denied != null) {
            return denied;
        }
        return AjaxResult.success(item);
    }

    /**
     * 新增团购商品
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:add')")
    @Log(title = "团购商品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GrouponActivityItem item) {
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null) {
            item.setMerchantId(effMerchantId);
        }
        AjaxResult denied = checkMerchantAccess(item.getMerchantId(), "团购商品");
        if (denied != null) {
            return denied;
        }
        denied = checkItemActivityRelation(item);
        if (denied != null) {
            return denied;
        }
        AjaxResult validateResult = validateItemForSave(item);
        if (validateResult != null) {
            return validateResult;
        }
        AjaxResult operateCheck = checkMerchantCanOperate(item.getMerchantId(), item.getStatus());
        if (operateCheck != null) {
            return operateCheck;
        }
        return toAjax(grouponActivityItemService.insertGrouponActivityItem(item));
    }

    /**
     * 修改团购商品
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:edit')")
    @Log(title = "团购商品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GrouponActivityItem item) {
        GrouponActivityItem existing = grouponActivityItemService.selectGrouponActivityItemById(item.getId());
        AjaxResult denied = checkItemAccess(existing, "团购商品");
        if (denied != null) {
            return denied;
        }
        AjaxResult operateCheck = checkMerchantCanOperate(existing.getMerchantId(), item.getStatus());
        if (operateCheck != null) {
            return operateCheck;
        }
        if (MallDataScopeHelper.currentEffectiveMerchantId() != null
                || MallDataScopeHelper.currentEffectiveDistributorId() != null) {
            item.setMerchantId(existing.getMerchantId());
            item.setGrouponId(existing.getGrouponId());
        } else {
            if (item.getMerchantId() == null) {
                item.setMerchantId(existing.getMerchantId());
            }
            if (item.getGrouponId() == null) {
                item.setGrouponId(existing.getGrouponId());
            }
            denied = checkItemActivityRelation(item);
            if (denied != null) {
                return denied;
            }
        }
        return toAjax(grouponActivityItemService.updateGrouponActivityItem(item));
    }

    /**
     * 删除团购商品
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:remove')")
    @Log(title = "团购商品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        for (Long id : ids) {
            GrouponActivityItem item = grouponActivityItemService.selectGrouponActivityItemById(id);
            AjaxResult denied = checkItemAccess(item, "团购商品");
            if (denied != null) {
                return denied;
            }
        }
        return toAjax(grouponActivityItemService.deleteGrouponActivityItemByIds(ids));
    }

    /**
     * 修改团购商品状态
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:edit')")
    @Log(title = "团购商品状态修改", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult changeStatus(@RequestBody GrouponActivityItem item) {
        GrouponActivityItem existing = grouponActivityItemService.selectGrouponActivityItemById(item.getId());
        AjaxResult denied = checkItemAccess(existing, "团购商品");
        if (denied != null) {
            return denied;
        }
        AjaxResult operateCheck = checkMerchantCanOperate(existing.getMerchantId(), item.getStatus());
        if (operateCheck != null) {
            return operateCheck;
        }
        GrouponActivityItem update = new GrouponActivityItem();
        update.setId(item.getId());
        update.setStatus(item.getStatus());
        return toAjax(grouponActivityItemService.updateGrouponActivityItem(update));
    }

    /**
     * 团购商品图片上传
     */
    @PreAuthorize("@ss.hasPermi('mall:groupon:edit')")
    @PostMapping("/image/upload")
    public AjaxResult uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("merchantId") Long merchantId,
            @RequestParam(value = "grouponId", required = false) Long grouponId,
            @RequestParam(value = "itemId", required = false) Long itemId,
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

        AjaxResult denied = checkMerchantAccess(merchantId, "团购商品图片");
        if (denied != null) {
            return denied;
        }
        if (grouponId != null) {
            GrouponActivity activity = grouponActivityService.selectGrouponActivityById(grouponId);
            denied = checkActivityAccess(activity, "团购商品图片");
            if (denied != null) {
                return denied;
            }
            if (!merchantId.equals(activity.getMerchantId())) {
                return AjaxResult.error("团购活动与商户不匹配");
            }
        }
        if (itemId != null) {
            GrouponActivityItem item = grouponActivityItemService.selectGrouponActivityItemById(itemId);
            denied = checkItemAccess(item, "团购商品图片");
            if (denied != null) {
                return denied;
            }
            if (!merchantId.equals(item.getMerchantId())) {
                return AjaxResult.error("团购商品与商户不匹配");
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

        String uploadRoot = RuoYiConfig.getProfile() + "/merchant_images";

        try {
            StringBuilder dirBuilder = new StringBuilder();
            dirBuilder.append(merchantId).append(File.separator);
            dirBuilder.append("groupon").append(File.separator);

            if (grouponId != null) {
                dirBuilder.append(grouponId).append(File.separator);
            } else {
                dirBuilder.append("temp").append(File.separator);
            }

            dirBuilder.append("item").append(File.separator);

            if (itemId != null) {
                dirBuilder.append(itemId).append(File.separator);
            } else if (tempToken != null) {
                if (!tempToken.matches("^[a-zA-Z0-9_-]{1,64}$")) {
                    return AjaxResult.error("无效的tempToken");
                }
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

    private AjaxResult validateItemForSave(GrouponActivityItem item) {
        if (item.getMerchantId() == null || item.getGrouponId() == null) {
            return AjaxResult.error("商户ID和活动ID不能为空");
        }
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            return AjaxResult.error("商品名称不能为空");
        }
        if (item.getOriginalPrice() == null || item.getOriginalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return AjaxResult.error("原价必须大于0");
        }
        if (item.getGrouponPrice() == null || item.getGrouponPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return AjaxResult.error("团购价必须大于0");
        }
        if (item.getGrouponPrice().compareTo(item.getOriginalPrice()) > 0) {
            return AjaxResult.error("团购价不能大于原价");
        }
        if (item.getStock() == null || item.getStock() < 0) {
            return AjaxResult.error("库存不能为负数");
        }
        if (item.getValidDays() == null || item.getValidDays() <= 0) {
            return AjaxResult.error("有效期必须大于0");
        }
        return null;
    }

    private AjaxResult checkItemActivityRelation(GrouponActivityItem item) {
        GrouponActivity activity = grouponActivityService.selectGrouponActivityById(item.getGrouponId());
        AjaxResult denied = checkActivityAccess(activity, "团购活动");
        if (denied != null) {
            return denied;
        }
        if (!item.getMerchantId().equals(activity.getMerchantId())) {
            return AjaxResult.error("团购商品与活动商户不匹配");
        }
        return null;
    }

    private AjaxResult checkActivityAccess(GrouponActivity activity, String label) {
        if (activity == null) {
            return AjaxResult.error(label + "不存在");
        }
        return checkMerchantAccess(activity.getMerchantId(), label);
    }

    private AjaxResult checkItemAccess(GrouponActivityItem item, String label) {
        if (item == null) {
            return AjaxResult.error(label + "不存在");
        }
        return checkMerchantAccess(item.getMerchantId(), label);
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
