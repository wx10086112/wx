package com.ruoyi.web.controller.mall;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mall.finance.domain.TransactionRecord;
import com.ruoyi.mall.finance.service.IFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mall/merchant")
public class MallMerchantFlowController extends BaseController {

    @Autowired
    private IFinanceService financeService;

    /**
     * 商家流水列表
     */
    @PreAuthorize("@ss.hasPermi('mall:merchant:list')")
    @GetMapping("/flow/list")
    public TableDataInfo flowList(@RequestParam(value = "merchantId", required = false) Long merchantId) {
        startPage();
        TransactionRecord query = new TransactionRecord();
        if (merchantId != null) {
            query.setMerchantId(merchantId);
        }
        return getDataTable(financeService.selectMerchantFlowList(query));
    }
}
