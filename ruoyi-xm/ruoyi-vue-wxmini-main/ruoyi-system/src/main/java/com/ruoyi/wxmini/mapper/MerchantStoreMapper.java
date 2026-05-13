package com.ruoyi.wxmini.mapper;

import com.ruoyi.wxmini.domain.MerchantStore;
import java.util.List;

public interface MerchantStoreMapper {
    MerchantStore selectMerchantStoreById(Long id);
    List<MerchantStore> selectMerchantStoreList(MerchantStore merchantStore);
    List<MerchantStore> selectMerchantStoreByMerchantId(Long merchantId);
    int insertMerchantStore(MerchantStore merchantStore);
    int updateMerchantStore(MerchantStore merchantStore);
    int deleteMerchantStoreById(Long id);
    int deleteMerchantStoreByIds(Long[] ids);
}
