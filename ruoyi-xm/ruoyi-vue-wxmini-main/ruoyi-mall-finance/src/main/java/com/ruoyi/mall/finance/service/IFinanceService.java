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
     * 查询商户流水列表
     */
    List<TransactionRecord> selectMerchantFlowList(TransactionRecord query);

    /**
     * 获取收入统计
     */
    Map<String, Object> getIncomeStats();

    /**
     * 获取财务报表
     */
    Map<String, Object> getReport();
}
