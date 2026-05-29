package com.ruoyi.mall.merchant.controller;

import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/mall/merchant")
public class MallMerchantController extends BaseController {

    @Autowired
    private IMerchantService merchantService;

    @DataScopeBiz(distributorAlias = "m")
    @PreAuthorize("@ss.hasPermi('mall:merchant:list')")
    @GetMapping("/list")
    public TableDataInfo list(Merchant merchant) {
        startPage();
        // 分销商账号只能看到自己名下的商家
        String accountType = SecurityUtils.getAccountType();
        if ("DISTRIBUTOR".equals(accountType)) {
            merchant.setDistributorId(SecurityUtils.getDistributorId());
        }
        List<Merchant> list = merchantService.selectMerchantList(merchant);
        // 脱敏处理：不返回密钥明文
        List<Map<String, Object>> safeList = new ArrayList<>();
        for (Merchant m : list) {
            safeList.add(toSafeMap(m));
        }
        return getDataTable(safeList);
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        Merchant merchant = merchantService.selectMerchantById(id);
        if (merchant == null) {
            return AjaxResult.error("商家不存在");
        }
        // 归属校验
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !effDistributorId.equals(merchant.getDistributorId())) {
            return AjaxResult.error("无权限查看该商家");
        }
        return AjaxResult.success(toSafeMap(merchant));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:add')")
    @Log(title = "商户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Merchant merchant) {
        // 分销商创建商家：强制归属到自己名下，忽略前端传值
        String accountType = SecurityUtils.getAccountType();
        if ("DISTRIBUTOR".equals(accountType)) {
            merchant.setDistributorId(SecurityUtils.getDistributorId());
        }
        return toAjax(merchantService.insertMerchant(merchant));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:edit')")
    @Log(title = "商户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Merchant merchant) {
        Merchant existing = merchantService.selectMerchantById(merchant.getId());
        if (existing == null) {
            return AjaxResult.error("商家不存在");
        }
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !effDistributorId.equals(existing.getDistributorId())) {
            return AjaxResult.error("无权限修改该商家");
        }
        // 过滤占位符，防止覆盖真实密钥
        if ("******".equals(merchant.getCMiniAppSecret())) {
            merchant.setCMiniAppSecret(null);
        }
        if ("******".equals(merchant.getMMiniAppSecret())) {
            merchant.setMMiniAppSecret(null);
        }
        if ("******".equals(merchant.getWxPayApiKey())) {
            merchant.setWxPayApiKey(null);
        }
        // 分销商不能修改商家归属，强制保留原 distributorId
        String accountType = SecurityUtils.getAccountType();
        if ("DISTRIBUTOR".equals(accountType)) {
            merchant.setDistributorId(existing.getDistributorId());
        }
        return toAjax(merchantService.updateMerchant(merchant));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:remove')")
    @Log(title = "商户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null) {
            for (Long id : ids) {
                Merchant m = merchantService.selectMerchantById(id);
                if (m != null && !effDistributorId.equals(m.getDistributorId())) {
                    return AjaxResult.error("无权限删除该商家");
                }
            }
        }
        return toAjax(merchantService.deleteMerchantByIds(ids));
    }

    /**
     * 将商家实体转为安全Map，敏感字段脱敏
     */
    private Map<String, Object> toSafeMap(Merchant m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("distributorId", m.getDistributorId());
        map.put("distributorName", m.getDistributorName());
        map.put("name", m.getName());
        map.put("logo", m.getLogo());
        map.put("contact", m.getContact());
        // 手机号脱敏
        String phone = m.getPhone();
        map.put("phone", phone != null && phone.length() >= 7
                ? phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4) : phone);
        map.put("commissionRate", m.getCommissionRate());
        map.put("status", m.getStatus());
        map.put("balance", m.getBalance());
        map.put("totalIncome", m.getTotalIncome());
        map.put("address", m.getAddress());
        map.put("avatar", m.getAvatar());
        map.put("description", m.getDescription());
        map.put("businessHours", m.getBusinessHours());
        map.put("productCount", m.getProductCount());
        map.put("storeCount", m.getStoreCount());
        map.put("createTime", m.getCreateTime());
        // 敏感字段脱敏
        map.put("cMiniAppId", m.getCMiniAppId());
        map.put("cMiniAppSecretConfigured", m.getCMiniAppSecret() != null && !m.getCMiniAppSecret().isEmpty());
        map.put("cMiniAppSecretMasked", "******");
        map.put("mMiniAppId", m.getMMiniAppId());
        map.put("mMiniAppSecretConfigured", m.getMMiniAppSecret() != null && !m.getMMiniAppSecret().isEmpty());
        map.put("mMiniAppSecretMasked", "******");
        map.put("wxPayMchId", m.getWxPayMchId());
        map.put("wxPayApiKeyConfigured", m.getWxPayApiKey() != null && !m.getWxPayApiKey().isEmpty());
        map.put("wxPayApiKeyMasked", "******");
        return map;
    }
}
