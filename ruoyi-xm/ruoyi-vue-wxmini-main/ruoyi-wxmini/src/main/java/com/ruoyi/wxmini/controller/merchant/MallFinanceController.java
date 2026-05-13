package com.ruoyi.wxmini.controller.merchant;

import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.wxmini.domain.PlatformIncome;
import com.ruoyi.wxmini.domain.TransactionRecord;
import com.ruoyi.wxmini.domain.WithdrawRecord;
import com.ruoyi.wxmini.service.IFinanceService;
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

    @PreAuthorize("@ss.hasPermi('mall:finance:list')")
    @GetMapping("/platform-flow/list")
    public TableDataInfo platformFlowList(TransactionRecord query) {
        startPage();
        List<TransactionRecord> list = financeService.selectPlatformFlowList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:finance:list')")
    @GetMapping("/profit-share/list")
    public TableDataInfo profitShareList(PlatformIncome query) {
        startPage();
        List<PlatformIncome> list = financeService.selectProfitShareList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:finance:list')")
    @GetMapping("/withdraw/list")
    public TableDataInfo withdrawList(WithdrawRecord query) {
        startPage();
        List<WithdrawRecord> list = financeService.selectWithdrawList(query);
        return getDataTable(list);
    }
}
