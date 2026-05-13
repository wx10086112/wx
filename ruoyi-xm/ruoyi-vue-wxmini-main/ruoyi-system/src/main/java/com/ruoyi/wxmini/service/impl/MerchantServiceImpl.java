package com.ruoyi.wxmini.service.impl;

import com.ruoyi.wxmini.domain.Merchant;
import com.ruoyi.wxmini.domain.TransactionRecord;
import com.ruoyi.wxmini.mapper.MerchantMapper;
import com.ruoyi.wxmini.mapper.TransactionRecordMapper;
import com.ruoyi.wxmini.service.IMerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantServiceImpl implements IMerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private TransactionRecordMapper transactionRecordMapper;

    @Override
    public Merchant selectMerchantById(Long id) {
        return merchantMapper.selectMerchantById(id);
    }

    @Override
    public List<Merchant> selectMerchantList(Merchant merchant) {
        return merchantMapper.selectMerchantList(merchant);
    }

    @Override
    public int insertMerchant(Merchant merchant) {
        return merchantMapper.insertMerchant(merchant);
    }

    @Override
    public int updateMerchant(Merchant merchant) {
        return merchantMapper.updateMerchant(merchant);
    }

    @Override
    public int deleteMerchantById(Long id) {
        return merchantMapper.deleteMerchantById(id);
    }

    @Override
    public int deleteMerchantByIds(Long[] ids) {
        return merchantMapper.deleteMerchantByIds(ids);
    }

    @Override
    public List<TransactionRecord> selectMerchantFlowList(TransactionRecord query) {
        return transactionRecordMapper.selectTransactionRecordList(query);
    }
}
