package com.ruoyi.mall.finance.service;

import com.ruoyi.mall.finance.domain.PlatformIncome;
import com.ruoyi.mall.finance.domain.TransactionRecord;

import java.util.List;
import java.util.Map;

public interface IFinanceService {

    /**
     * 查询平台流水列表
     */
    List<TransactionRecord> selectPlatformFlowList(TransactionRecord transactionRecord);

    /**
     * 查询利润分成列表
     */
    List<PlatformIncome> selectProfitShareList(PlatformIncome platformIncome);

    /**
     * 查询提现记录列表
     */
    List selectWithdrawList(com.ruoyi.mall.finance.domain.WithdrawRecord withdrawRecord);

    /**
     * 查询商户流水列表
     */
    List<TransactionRecord> selectMerchantFlowList(TransactionRecord query);

    /**
     * 审批提现申请
     */
    boolean approveWithdraw(Long id, Integer status);

    /**
     * 获取收入统计
     */
    Map<String, Object> getIncomeStats();

    /**
     * 获取财务报表
     */
    Map<String, Object> getReport();
}
