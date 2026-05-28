package com.ruoyi.mall.finance.service.impl;

import com.ruoyi.mall.finance.domain.PlatformIncome;
import com.ruoyi.mall.finance.domain.TransactionRecord;
import com.ruoyi.mall.finance.domain.WithdrawRecord;
import com.ruoyi.mall.finance.mapper.PlatformIncomeMapper;
import com.ruoyi.mall.finance.mapper.TransactionRecordMapper;
import com.ruoyi.mall.finance.mapper.WithdrawRecordMapper;
import com.ruoyi.mall.finance.service.IFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<WithdrawRecord> selectWithdrawList(WithdrawRecord withdrawRecord) {
        return withdrawRecordMapper.selectWithdrawRecordList(withdrawRecord);
    }

    @Override
    public List<TransactionRecord> selectMerchantFlowList(TransactionRecord query) {
        return transactionRecordMapper.selectTransactionRecordList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveWithdraw(Long id, Integer status) {
        WithdrawRecord record = withdrawRecordMapper.selectWithdrawRecordById(id);
        if (record == null) {
            return false;
        }
        record.setStatus(status);
        record.setAuditTime(new Date());
        withdrawRecordMapper.updateWithdrawRecord(record);
        return true;
    }

    @Override
    public Map<String, Object> getIncomeStats() {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalCommission = platformIncomeMapper.sumTotalCommission();
        BigDecimal todayIncome = transactionRecordMapper.sumTodayByType(1);
        BigDecimal monthIncome = transactionRecordMapper.sumMonthByType(1);
        BigDecimal totalWithdraw = withdrawRecordMapper.sumPaidTotal();
        stats.put("totalCommission", totalCommission);
        stats.put("todayIncome", todayIncome);
        stats.put("monthIncome", monthIncome);
        stats.put("totalWithdraw", totalWithdraw);
        return stats;
    }

    @Override
    public Map<String, Object> getReport() {
        Map<String, Object> report = new HashMap<>();
        List<Map> monthlyReport = transactionRecordMapper.selectMonthlyReport();
        report.put("monthlyReport", monthlyReport);
        return report;
    }
}
