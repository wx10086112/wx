package com.ruoyi.mall.merchant.controller;

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
public class MallMerchantController extends BaseController {

    @Autowired
    private IMerchantService merchantService;

    @PreAuthorize("@ss.hasPermi('mall:merchant:list')")
    @GetMapping("/list")
    public TableDataInfo list(Merchant merchant) {
        startPage();
        List<Merchant> list = merchantService.selectMerchantList(merchant);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return AjaxResult.success(merchantService.selectMerchantById(id));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:add')")
    @Log(title = "商户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Merchant merchant) {
        return toAjax(merchantService.insertMerchant(merchant));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:edit')")
    @Log(title = "商户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Merchant merchant) {
        return toAjax(merchantService.updateMerchant(merchant));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:remove')")
    @Log(title = "商户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(merchantService.deleteMerchantByIds(ids));
    }
}
