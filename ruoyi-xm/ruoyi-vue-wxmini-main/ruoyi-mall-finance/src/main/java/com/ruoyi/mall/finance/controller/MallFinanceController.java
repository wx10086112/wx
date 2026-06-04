package com.ruoyi.mall.finance.controller;

import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mall.finance.domain.PlatformIncome;
import com.ruoyi.mall.finance.domain.TransactionRecord;
import com.ruoyi.mall.finance.service.IFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mall/finance")
public class MallFinanceController extends BaseController {

    @Autowired
    private IFinanceService financeService;

    /**
     * 查询平台流水列表
     */
    @DataScopeBiz(merchantAlias = "t", distributorAlias = "m")
    @PreAuthorize("@ss.hasPermi('mall:finance:list')")
    @GetMapping("/platform-flow/list")
    public TableDataInfo platformFlowList(TransactionRecord query) {
        startPage();
        List<TransactionRecord> list = financeService.selectPlatformFlowList(query);
        return getDataTable(list);
    }

    /**
     * 查询利润分成列表
     */
    @DataScopeBiz(merchantAlias = "p", distributorAlias = "m")
    @PreAuthorize("@ss.hasPermi('mall:finance:list')")
    @GetMapping("/profit-share/list")
    public TableDataInfo profitShareList(PlatformIncome query) {
        startPage();
        List<PlatformIncome> list = financeService.selectProfitShareList(query);
        return getDataTable(list);
    }

    /**
     * 查询商户流水列表
     */
    @DataScopeBiz(merchantAlias = "t", distributorAlias = "m")
    @PreAuthorize("@ss.hasPermi('mall:finance:list')")
    @GetMapping("/merchant-flow/list")
    public TableDataInfo merchantFlowList(TransactionRecord query) {
        startPage();
        List<TransactionRecord> list = financeService.selectMerchantFlowList(query);
        return getDataTable(list);
    }
}
