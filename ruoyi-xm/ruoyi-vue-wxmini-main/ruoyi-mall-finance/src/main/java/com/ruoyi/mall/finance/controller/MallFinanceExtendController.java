package com.ruoyi.mall.finance.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.finance.service.IFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mall/finance")
public class MallFinanceExtendController extends BaseController {

    @Autowired
    private IFinanceService financeService;

    /**
     * 审批提现申请
     */
    @PreAuthorize("@ss.hasPermi('mall:finance:edit')")
    @PostMapping("/withdraw/approve/{id}/{status}")
    public AjaxResult approveWithdraw(@PathVariable Long id, @PathVariable Integer status) {
        boolean ok = financeService.approveWithdraw(id, status);
        return ok ? AjaxResult.success() : AjaxResult.error("提现记录不存在");
    }

    /**
     * 获取收入统计
     */
    @PreAuthorize("@ss.hasPermi('mall:finance:list')")
    @GetMapping("/income/stats")
    public AjaxResult incomeStats() {
        return AjaxResult.success(financeService.getIncomeStats());
    }

    /**
     * 获取财务报表
     */
    @PreAuthorize("@ss.hasPermi('mall:finance:list')")
    @GetMapping("/report")
    public AjaxResult report() {
        return AjaxResult.success(financeService.getReport());
    }
}
