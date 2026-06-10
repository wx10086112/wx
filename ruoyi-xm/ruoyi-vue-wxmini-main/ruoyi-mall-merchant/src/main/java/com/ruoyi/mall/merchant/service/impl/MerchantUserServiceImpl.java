package com.ruoyi.mall.merchant.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.merchant.domain.MerchantUser;
import com.ruoyi.mall.merchant.mapper.MerchantUserMapper;
import com.ruoyi.mall.merchant.service.IMerchantUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantUserServiceImpl implements IMerchantUserService {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    private MerchantUserMapper merchantUserMapper;

    @Override
    public MerchantUser selectMerchantUserById(Long id) {
        return merchantUserMapper.selectMerchantUserById(id);
    }

    @Override
    public List<MerchantUser> selectMerchantUserList(MerchantUser merchantUser) {
        return merchantUserMapper.selectMerchantUserList(merchantUser);
    }

    @Override
    public int insertMerchantUser(MerchantUser merchantUser) {
        if (StringUtils.isBlank(merchantUser.getPassword())) {
            throw new ServiceException("商家用户初始密码不能为空");
        }
        // 加密密码
        merchantUser.setPassword(encoder.encode(merchantUser.getPassword()));
        if (merchantUser.getStatus() == null) {
            merchantUser.setStatus(1);
        }
        if (merchantUser.getRole() == null) {
            merchantUser.setRole("member");
        }
        return merchantUserMapper.insertMerchantUser(merchantUser);
    }

    @Override
    public int updateMerchantUser(MerchantUser merchantUser) {
        // 不更新密码字段
        merchantUser.setPassword(null);
        return merchantUserMapper.updateMerchantUser(merchantUser);
    }

    @Override
    public int deleteMerchantUserByIds(Long[] ids) {
        return merchantUserMapper.deleteMerchantUserByIds(ids);
    }

    @Override
    public boolean checkUsernameUnique(String username, Long excludeId) {
        MerchantUser existing = merchantUserMapper.selectMerchantUserByUsername(username);
        if (existing == null) {
            return true;
        }
        return excludeId != null && existing.getId().equals(excludeId);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        if (StringUtils.isBlank(newPassword)) {
            throw new ServiceException("新密码不能为空");
        }
        MerchantUser user = new MerchantUser();
        user.setId(id);
        user.setPassword(encoder.encode(newPassword));
        merchantUserMapper.updateMerchantUser(user);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        MerchantUser user = new MerchantUser();
        user.setId(id);
        user.setStatus(status);
        merchantUserMapper.updateMerchantUser(user);
    }
}
