package com.ruoyi.mall.merchant.service;

import com.ruoyi.mall.merchant.domain.Merchant;
import java.util.List;

public interface IMerchantService {

    Merchant selectMerchantById(Long id);

    List<Merchant> selectMerchantList(Merchant merchant);

    int insertMerchant(Merchant merchant);

    int updateMerchant(Merchant merchant);

    int deleteMerchantById(Long id);

    int deleteMerchantByIds(Long[] ids);

    Merchant selectMerchantByCAppId(String cMiniAppId);

    Merchant selectMerchantByMAppId(String mMiniAppId);
}
