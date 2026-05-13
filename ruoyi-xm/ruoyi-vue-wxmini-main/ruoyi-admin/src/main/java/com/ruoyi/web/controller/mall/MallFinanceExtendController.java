package com.ruoyi.web.controller.mall;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.wxmini.domain.WithdrawRecord;
import com.ruoyi.wxmini.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

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
     * 审批提现
     * status: 1=通过, 0=拒绝
     */
    @PreAuthorize("@ss.hasPermi('mall:finance:edit')")
    @PostMapping("/withdraw/approve/{id}/{status}")
    public AjaxResult approveWithdraw(@PathVariable Long id, @PathVariable Integer status) {
        WithdrawRecord record = withdrawRecordMapper.selectWithdrawRecordById(id);
        if (record == null) {
            return error("提现记录不存在");
        }
        record.setStatus(status == 1 ? 2 : 3);
        return toAjax(withdrawRecordMapper.updateWithdrawRecord(record));
    }

    /**
     * 收益统计
     */
    @PreAuthorize("@ss.hasPermi('mall:finance:list')")
    @GetMapping("/income/stats")
    public AjaxResult incomeStats() {
        Map<String, Object> stats = new HashMap<>();
        // type: 1=支付收入, 5=佣金
        stats.put("todayIncome", transactionRecordMapper.sumTodayByType(1));
        stats.put("monthIncome", transactionRecordMapper.sumMonthByType(1));
        stats.put("totalIncome", transactionRecordMapper.sumAmountByType(1));
        stats.put("todayCommission", transactionRecordMapper.sumTodayByType(5));
        stats.put("monthCommission", transactionRecordMapper.sumMonthByType(5));
        stats.put("totalCommission", platformIncomeMapper.sumTotalCommission());
        return success(stats);
    }

    /**
     * 财务报表
     */
    @PreAuthorize("@ss.hasPermi('mall:finance:list')")
    @GetMapping("/report")
    public AjaxResult report() {
        Map<String, Object> report = new HashMap<>();
        // type: 1=支付收入
        BigDecimal totalRevenue = transactionRecordMapper.sumAmountByType(1);
        BigDecimal totalCommission = platformIncomeMapper.sumTotalCommission();
        BigDecimal totalRefund = refundRecordMapper.sumRefundTotal();
        BigDecimal totalWithdraw = withdrawRecordMapper.sumPaidTotal();
        BigDecimal netProfit = totalRevenue.subtract(totalCommission).subtract(totalRefund);

        // 月度数据
        List<Map<String, Object>> monthlyData = transactionRecordMapper.selectMonthlyReport();

        report.put("totalRevenue", totalRevenue);
        report.put("totalCommission", totalCommission);
        report.put("totalWithdraw", totalWithdraw);
        report.put("totalRefund", totalRefund);
        report.put("netProfit", netProfit);
        report.put("monthlyData", monthlyData);
        return success(report);
    }
}
