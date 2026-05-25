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

import java.util.List;

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
}
