package com.ruoyi.mall.merchant.service;

import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.domain.MerchantStore;
import java.util.List;
import java.util.Map;

public interface IMerchantService {

    Merchant selectMerchantById(Long id);

    Map<String, Object> selectMerchantLiveStats(Long merchantId);

    List<Map<String, Object>> selectMerchantLiveStatsBatch(List<Long> merchantIds);

    Merchant selectMerchantByIdAnyStatus(Long id);

    List<Merchant> selectMerchantList(Merchant merchant);

    int insertMerchant(Merchant merchant);

    int updateMerchant(Merchant merchant);

    MerchantStore selectPrimaryStoreByMerchantId(Long merchantId);

    MerchantStore createPrimaryStore(Long merchantId, MerchantStore merchantStore);

    MerchantStore updatePrimaryStore(Long merchantId, MerchantStore merchantStore);

    int deleteMerchantById(Long id);

    int deleteMerchantByIds(Long[] ids);

    int clearDistributorBindingsByDistributorIds(Long[] ids);

    int clearRevivedDistributorBindings(Long distributorId);

    Merchant selectMerchantByCAppId(String cMiniAppId);

    Merchant selectMerchantByMAppId(String mMiniAppId);

    Merchant selectMerchantByAnyMiniAppId(String miniAppId);
}
