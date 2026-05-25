package com.ruoyi.mall.merchant.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.domain.MerchantUser;
import com.ruoyi.mall.merchant.mapper.MerchantMapper;
import com.ruoyi.mall.merchant.mapper.MerchantUserMapper;
import com.ruoyi.mall.merchant.service.IMerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MerchantServiceImpl implements IMerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private MerchantUserMapper merchantUserMapper;

    @Override
    public Merchant selectMerchantById(Long id) {
        return merchantMapper.selectMerchantById(id);
    }

    @Override
    public List<Merchant> selectMerchantList(Merchant merchant) {
        return merchantMapper.selectMerchantList(merchant);
    }

    @Override
    @Transactional
    public int insertMerchant(Merchant merchant) {
        int rows = merchantMapper.insertMerchant(merchant);
        if (rows > 0 && merchant.getId() != null) {
            MerchantUser owner = new MerchantUser();
            owner.setMerchantId(merchant.getId());
            owner.setUsername(merchant.getPhone() != null ? merchant.getPhone() : "merchant_" + merchant.getId());
            owner.setPassword(SecurityUtils.encryptPassword("123456"));
            owner.setRealName(merchant.getContact() != null ? merchant.getContact() : "管理员");
            owner.setPhone(merchant.getPhone());
            owner.setRole("owner");
            owner.setStatus(1);
            merchantUserMapper.insertMerchantUser(owner);
        }
        return rows;
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
    public Merchant selectMerchantByCAppId(String cMiniAppId) {
        return merchantMapper.selectMerchantByCAppId(cMiniAppId);
    }

    @Override
    public Merchant selectMerchantByMAppId(String mMiniAppId) {
        return merchantMapper.selectMerchantByMAppId(mMiniAppId);
    }
}
