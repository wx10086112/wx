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

import java.math.BigDecimal;
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
    @Transactional(rollbackFor = Exception.class)
    public int insertMerchant(Merchant merchant) {
        // 新增商户默认正常状态，无需审核
        if (merchant.getStatus() == null) {
            merchant.setStatus(1);
        }
        fillPaymentDefaults(merchant);
        int rows = merchantMapper.insertMerchant(merchant);
        if (rows > 0 && merchant.getId() != null) {
            ensureMerchantOwnerAccount(merchant);
        }
        return rows;
    }

    @Override
    public int updateMerchant(Merchant merchant) {
        return merchantMapper.updateMerchant(merchant);
    }

    private void fillPaymentDefaults(Merchant merchant) {
        if (merchant.getWxPaymentAccessType() == null) {
            merchant.setWxPaymentAccessType("EXISTING_MCH");
        }
        if (merchant.getWxProfitSharingEnabled() == null) {
            merchant.setWxProfitSharingEnabled(0);
        }
        if (merchant.getMerchantShareRate() == null) {
            merchant.setMerchantShareRate(new BigDecimal("100.00"));
        }
        if (merchant.getPlatformShareRate() == null) {
            merchant.setPlatformShareRate(BigDecimal.ZERO);
        }
        if (merchant.getDistributorShareRate() == null) {
            merchant.setDistributorShareRate(BigDecimal.ZERO);
        }
        if (merchant.getSettlementCycle() == null) {
            merchant.setSettlementCycle("T1");
        }
    }

    @org.springframework.beans.factory.annotation.Value("${mall.merchant.defaultPassword:123456}")
    private String defaultPassword;

    private void ensureMerchantOwnerAccount(Merchant merchant) {
        List<MerchantUser> merchantUsers = merchantUserMapper.selectMerchantUserByMerchantId(merchant.getId());
        for (MerchantUser merchantUser : merchantUsers) {
            if ("owner".equalsIgnoreCase(merchantUser.getRole())) {
                return;
            }
        }

        MerchantUser owner = new MerchantUser();
        owner.setMerchantId(merchant.getId());
        owner.setUsername(buildDefaultOwnerUsername(merchant.getId()));
        owner.setPassword(SecurityUtils.encryptPassword(defaultPassword));
        owner.setRealName(resolveOwnerRealName(merchant));
        owner.setPhone(merchant.getPhone());
        owner.setRole("owner");
        owner.setStatus(1);
        owner.setRemark("系统自动创建的商家超级用户");
        merchantUserMapper.insertMerchantUser(owner);
    }

    private String buildDefaultOwnerUsername(Long merchantId) {
        return "merchant_" + merchantId + "_owner";
    }

    private String resolveOwnerRealName(Merchant merchant) {
        if (merchant.getContact() != null && !merchant.getContact().trim().isEmpty()) {
            return merchant.getContact();
        }
        if (merchant.getName() != null && !merchant.getName().trim().isEmpty()) {
            return merchant.getName() + "超级管理员";
        }
        return "商家超级管理员";
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
    public Merchant selectMerchantByIdAnyStatus(Long id) {
        return merchantMapper.selectMerchantByIdAnyStatus(id);
    }

    @Override
    public int clearDistributorBindingsByDistributorIds(Long[] ids) {
        return merchantMapper.clearDistributorBindingsByDistributorIds(ids);
    }

    @Override
    public int clearRevivedDistributorBindings(Long distributorId) {
        return merchantMapper.clearRevivedDistributorBindings(distributorId);
    }

    @Override
    public Merchant selectMerchantByCAppId(String cMiniAppId) {
        return merchantMapper.selectMerchantByCAppId(cMiniAppId);
    }

    @Override
    public Merchant selectMerchantByMAppId(String mMiniAppId) {
        return merchantMapper.selectMerchantByMAppId(mMiniAppId);
    }

    @Override
    public Merchant selectMerchantByAnyMiniAppId(String miniAppId) {
        return merchantMapper.selectMerchantByAnyMiniAppId(miniAppId);
    }
}
