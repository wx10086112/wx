package com.ruoyi.mall.merchant.service;

import com.ruoyi.mall.merchant.domain.MerchantUser;
import java.util.List;

public interface IMerchantUserService {

    MerchantUser selectMerchantUserById(Long id);

    List<MerchantUser> selectMerchantUserList(MerchantUser merchantUser);

    int insertMerchantUser(MerchantUser merchantUser);

    int updateMerchantUser(MerchantUser merchantUser);

    int deleteMerchantUserByIds(Long[] ids);

    boolean checkUsernameUnique(String username, Long excludeId);

    void resetPassword(Long id, String newPassword);

    void changeStatus(Long id, Integer status);
}
