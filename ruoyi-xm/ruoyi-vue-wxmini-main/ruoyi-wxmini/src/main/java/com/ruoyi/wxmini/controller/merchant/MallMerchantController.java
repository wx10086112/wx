package com.ruoyi.wxmini.controller.merchant;

import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.wxmini.domain.Merchant;
import com.ruoyi.wxmini.domain.TransactionRecord;
import com.ruoyi.wxmini.service.IMerchantService;
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
        return success(merchantService.selectMerchantById(id));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:add')")
    @PostMapping
    public AjaxResult add(@RequestBody Merchant merchant) {
        return toAjax(merchantService.insertMerchant(merchant));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody Merchant merchant) {
        return toAjax(merchantService.updateMerchant(merchant));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(merchantService.deleteMerchantByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('mall:merchant:list')")
    @GetMapping("/flow/list")
    public TableDataInfo flowList(TransactionRecord query) {
        startPage();
        List<TransactionRecord> list = merchantService.selectMerchantFlowList(query);
        return getDataTable(list);
    }
}
