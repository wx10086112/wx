package com.ruoyi.mall.finance.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.finance.mapper.PlatformIncomeMapper;
import com.ruoyi.mall.finance.mapper.TransactionRecordMapper;
import com.ruoyi.mall.finance.mapper.WithdrawRecordMapper;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mall/finance")
public class MallFinanceExtendController extends BaseController {

    @Autowired
    private WithdrawRecordMapper withdrawRecordMapper;

    @Autowired
    private TransactionRecordMapper transactionRecordMapper;

    @Autowired
    private PlatformIncomeMapper platformIncomeMapper;

    @Autowired
    private RefundRecordMapper refundRecordMapper;

    /**
     * 审批提现申请
     */
    @PreAuthorize("@ss.hasPermi('mall:finance:edit')")
    @PostMapping("/withdraw/approve/{id}/{status}")
    public AjaxResult approveWithdraw(@PathVariable Long id, @PathVariable Integer status) {
        com.ruoyi.mall.finance.domain.WithdrawRecord record = withdrawRecordMapper.selectWithdrawRecordById(id);
        if (record == null) {
            return AjaxResult.error("提现记录不存在");
        }
        record.setStatus(status);
        record.setAuditTime(new Date());
        withdrawRecordMapper.updateWithdrawRecord(record);
        return AjaxResult.success();
    }

    /**
     * 获取收入统计
     */
    @PreAuthorize("@ss.hasPermi('mall:finance:list')")
    @GetMapping("/income/stats")
    public AjaxResult incomeStats() {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalCommission = platformIncomeMapper.sumTotalCommission();
        BigDecimal todayIncome = transactionRecordMapper.sumTodayByType(1);
        BigDecimal monthIncome = transactionRecordMapper.sumMonthByType(1);
        BigDecimal totalWithdraw = withdrawRecordMapper.sumPaidTotal();
        stats.put("totalCommission", totalCommission);
        stats.put("todayIncome", todayIncome);
        stats.put("monthIncome", monthIncome);
        stats.put("totalWithdraw", totalWithdraw);
        return AjaxResult.success(stats);
    }

    /**
     * 获取财务报表
     */
    @PreAuthorize("@ss.hasPermi('mall:finance:list')")
    @GetMapping("/report")
    public AjaxResult report() {
        Map<String, Object> report = new HashMap<>();
        List<Map> monthlyReport = transactionRecordMapper.selectMonthlyReport();
        report.put("monthlyReport", monthlyReport);
        return AjaxResult.success(report);
    }
}
