package com.ruoyi.mall.merchant.mapper;

import com.ruoyi.mall.merchant.domain.MerchantUser;
import java.util.List;

public interface MerchantUserMapper {
    MerchantUser selectMerchantUserById(Long id);
    MerchantUser selectMerchantUserByUsername(String username);
    List<MerchantUser> selectMerchantUserList(MerchantUser merchantUser);
    List<MerchantUser> selectMerchantUserByMerchantId(Long merchantId);
    int insertMerchantUser(MerchantUser merchantUser);
    int updateMerchantUser(MerchantUser merchantUser);
    int deleteMerchantUserById(Long id);
    int deleteMerchantUserByIds(Long[] ids);
}
