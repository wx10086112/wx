package com.ruoyi.wxmini.service;

import com.ruoyi.wxmini.domain.PlatformIncome;
import com.ruoyi.wxmini.domain.TransactionRecord;
import com.ruoyi.wxmini.domain.WithdrawRecord;
import java.util.List;

public interface IFinanceService {
    List<TransactionRecord> selectPlatformFlowList(TransactionRecord query);
    List<PlatformIncome> selectProfitShareList(PlatformIncome query);
    List<WithdrawRecord> selectWithdrawList(WithdrawRecord query);
}
