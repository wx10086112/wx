package com.ruoyi.wxmini.mapper;

import com.ruoyi.wxmini.domain.Merchant;
import java.util.List;

public interface MerchantMapper {
    Merchant selectMerchantById(Long id);
    List<Merchant> selectMerchantList(Merchant merchant);
    int insertMerchant(Merchant merchant);
    int updateMerchant(Merchant merchant);
    int deleteMerchantById(Long id);
    int deleteMerchantByIds(Long[] ids);
}
