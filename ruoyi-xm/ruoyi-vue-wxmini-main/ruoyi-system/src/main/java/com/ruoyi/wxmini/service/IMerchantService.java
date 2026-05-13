package com.ruoyi.wxmini.service;

import com.ruoyi.wxmini.domain.Merchant;
import com.ruoyi.wxmini.domain.TransactionRecord;
import java.util.List;

public interface IMerchantService {
    Merchant selectMerchantById(Long id);
    List<Merchant> selectMerchantList(Merchant merchant);
    int insertMerchant(Merchant merchant);
    int updateMerchant(Merchant merchant);
    int deleteMerchantById(Long id);
    int deleteMerchantByIds(Long[] ids);
    List<TransactionRecord> selectMerchantFlowList(TransactionRecord query);
}
