package com.ruoyi.wxmini.service.impl;

import com.ruoyi.wxmini.domain.PlatformIncome;
import com.ruoyi.wxmini.domain.TransactionRecord;
import com.ruoyi.wxmini.domain.WithdrawRecord;
import com.ruoyi.wxmini.mapper.PlatformIncomeMapper;
import com.ruoyi.wxmini.mapper.TransactionRecordMapper;
import com.ruoyi.wxmini.mapper.WithdrawRecordMapper;
import com.ruoyi.wxmini.service.IFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinanceServiceImpl implements IFinanceService {

    @Autowired
    private TransactionRecordMapper transactionRecordMapper;

    @Autowired
    private PlatformIncomeMapper platformIncomeMapper;

    @Autowired
    private WithdrawRecordMapper withdrawRecordMapper;

    @Override
    public List<TransactionRecord> selectPlatformFlowList(TransactionRecord query) {
        // 平台流水 = merchant_id 为 null 的交易记录
        query.setMerchantId(null);
        return transactionRecordMapper.selectTransactionRecordList(query);
    }

    @Override
    public List<PlatformIncome> selectProfitShareList(PlatformIncome query) {
        return platformIncomeMapper.selectPlatformIncomeList(query);
    }

    @Override
    public List<WithdrawRecord> selectWithdrawList(WithdrawRecord query) {
        return withdrawRecordMapper.selectWithdrawRecordList(query);
    }
}
