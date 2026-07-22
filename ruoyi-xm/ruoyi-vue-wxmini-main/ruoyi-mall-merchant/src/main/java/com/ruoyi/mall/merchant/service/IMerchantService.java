package com.ruoyi.mall.merchant.service;

import com.ruoyi.mall.merchant.domain.Merchant;
import java.util.List;
import java.util.Map;

public interface IMerchantService {

    Merchant selectMerchantById(Long id);

    Map<String, Object> selectMerchantLiveStats(Long merchantId);

    Merchant selectMerchantByIdAnyStatus(Long id);

    List<Merchant> selectMerchantList(Merchant merchant);

    int insertMerchant(Merchant merchant);

    int updateMerchant(Merchant merchant);

    int deleteMerchantById(Long id);

    int deleteMerchantByIds(Long[] ids);

    int clearDistributorBindingsByDistributorIds(Long[] ids);

    int clearRevivedDistributorBindings(Long distributorId);

    Merchant selectMerchantByCAppId(String cMiniAppId);

    Merchant selectMerchantByMAppId(String mMiniAppId);

    Merchant selectMerchantByAnyMiniAppId(String miniAppId);
}
