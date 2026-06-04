package com.ruoyi.mall.finance.service.impl;

import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.mall.finance.domain.PlatformIncome;
import com.ruoyi.mall.finance.domain.TransactionRecord;
import com.ruoyi.mall.finance.mapper.PlatformIncomeMapper;
import com.ruoyi.mall.finance.mapper.TransactionRecordMapper;
import com.ruoyi.mall.finance.mapper.WithdrawRecordMapper;
import com.ruoyi.mall.finance.service.IFinanceService;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class FinanceServiceImpl implements IFinanceService {

    @Autowired
    private TransactionRecordMapper transactionRecordMapper;

    @Autowired
    private WithdrawRecordMapper withdrawRecordMapper;

    @Autowired
    private PlatformIncomeMapper platformIncomeMapper;

    @Autowired
    private IMerchantService merchantService;

    @Override
    public List<TransactionRecord> selectPlatformFlowList(TransactionRecord transactionRecord) {
        transactionRecord.setMerchantId(null);
        return transactionRecordMapper.selectTransactionRecordList(transactionRecord);
    }

    @Override
    public List<PlatformIncome> selectProfitShareList(PlatformIncome platformIncome) {
        return platformIncomeMapper.selectPlatformIncomeList(platformIncome);
    }

    @Override
    public List<TransactionRecord> selectMerchantFlowList(TransactionRecord query) {
        return transactionRecordMapper.selectTransactionRecordList(query);
    }

    @Override
    public Map<String, Object> getIncomeStats() {
        Map<String, Object> stats = new HashMap<>();
        Long merchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        Long distributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        BigDecimal totalCommission = platformIncomeMapper.sumTotalCommissionScoped(merchantId, distributorId);
        BigDecimal todayIncome = transactionRecordMapper.sumTodayByTypeScoped(1, merchantId, distributorId);
        BigDecimal monthIncome = transactionRecordMapper.sumMonthByTypeScoped(1, merchantId, distributorId);
        BigDecimal totalWithdraw = withdrawRecordMapper.sumPaidTotalScoped(merchantId, distributorId);
        stats.put("totalCommission", totalCommission);
        stats.put("todayIncome", todayIncome);
        stats.put("monthIncome", monthIncome);
        stats.put("totalWithdraw", totalWithdraw);
        return stats;
    }

    @Override
    public Map<String, Object> getReport() {
        Map<String, Object> report = new HashMap<>();
        Long merchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        Long distributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        List<Map> monthlyReport = transactionRecordMapper.selectMonthlyReportScoped(merchantId, distributorId);
        report.put("monthlyReport", monthlyReport);
        return report;
    }
}
