package com.ruoyi.mall.merchant.controller;

import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mall/merchant")
public class MallMerchantAuditController extends BaseController {

    @Autowired
    private IMerchantService merchantService;

    @DataScopeBiz(distributorAlias = "m")
    @PreAuthorize("@ss.hasPermi('mall:merchant:audit')")
    @GetMapping("/audit/list")
    public TableDataInfo auditList(Merchant merchant) {
        startPage();
        List<Merchant> list = merchantService.selectMerchantList(merchant);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:audit')")
    @Log(title = "商户审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{id}/{status}")
    public AjaxResult audit(@PathVariable Long id, @PathVariable Integer status) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setStatus(status);
        return toAjax(merchantService.updateMerchant(merchant));
    }
}
