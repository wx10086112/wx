package com.ruoyi.mall.merchant.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.domain.MerchantStore;
import com.ruoyi.mall.merchant.domain.MerchantUser;
import com.ruoyi.mall.merchant.mapper.MerchantMapper;
import com.ruoyi.mall.merchant.mapper.MerchantStoreMapper;
import com.ruoyi.mall.merchant.mapper.MerchantUserMapper;
import com.ruoyi.mall.merchant.service.IMerchantService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class MerchantServiceImpl implements IMerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private MerchantUserMapper merchantUserMapper;

    @Autowired
    private MerchantStoreMapper merchantStoreMapper;

    @Override
    public Merchant selectMerchantById(Long id) {
        return merchantMapper.selectMerchantById(id);
    }

    @Override
    public Map<String, Object> selectMerchantLiveStats(Long merchantId) {
        return merchantMapper.selectMerchantLiveStats(merchantId);
    }

    @Override
    public List<Map<String, Object>> selectMerchantLiveStatsBatch(List<Long> merchantIds) {
        if (merchantIds == null || merchantIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return merchantMapper.selectMerchantLiveStatsBatch(merchantIds);
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
            insertPrimaryStore(merchant, buildDefaultPrimaryStore(merchant));
        }
        return rows;
    }

    @Override
    public int updateMerchant(Merchant merchant) {
        return merchantMapper.updateMerchant(merchant);
    }

    @Override
    public MerchantStore selectPrimaryStoreByMerchantId(Long merchantId) {
        List<MerchantStore> stores = merchantStoreMapper.selectMerchantStoreByMerchantId(merchantId);
        return stores.isEmpty() ? null : stores.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantStore createPrimaryStore(Long merchantId, MerchantStore merchantStore) {
        if (merchantId == null || merchantMapper.selectMerchantIdForUpdate(merchantId) == null) {
            throw new IllegalArgumentException("商家不存在");
        }
        Merchant merchant = requireMerchant(merchantId);
        if (merchantStoreMapper.countMerchantStoreByMerchantId(merchantId) > 0) {
            throw new IllegalArgumentException("该商家已经存在门店，当前阶段只支持一家主门店");
        }
        MerchantStore store = merchantStore == null ? new MerchantStore() : merchantStore;
        insertPrimaryStore(merchant, store);
        return merchantStoreMapper.selectMerchantStoreById(store.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantStore updatePrimaryStore(Long merchantId, MerchantStore merchantStore) {
        requireMerchant(merchantId);
        MerchantStore existing = selectPrimaryStoreByMerchantId(merchantId);
        if (existing == null) {
            throw new IllegalArgumentException("该商家尚未创建主门店");
        }
        if (merchantStore == null) {
            throw new IllegalArgumentException("门店信息不能为空");
        }
        merchantStore.setId(existing.getId());
        merchantStore.setMerchantId(merchantId);
        merchantStore.setIsMain(1);
        normalizeNullableStoreFields(merchantStore);
        validateStore(merchantStore);
        if (merchantStoreMapper.updatePrimaryMerchantStore(merchantStore) <= 0) {
            throw new IllegalArgumentException("门店信息保存失败");
        }
        syncStoreCount(merchantId);
        return merchantStoreMapper.selectMerchantStoreById(existing.getId());
    }

    private Merchant requireMerchant(Long merchantId) {
        Merchant merchant = merchantId == null ? null : merchantMapper.selectMerchantById(merchantId);
        if (merchant == null) {
            throw new IllegalArgumentException("商家不存在");
        }
        return merchant;
    }

    private MerchantStore buildDefaultPrimaryStore(Merchant merchant) {
        MerchantStore store = new MerchantStore();
        store.setName(merchant.getName());
        store.setContact(merchant.getContact());
        store.setPhone(merchant.getPhone());
        store.setAddress(merchant.getAddress());
        store.setBusinessHours(merchant.getBusinessHours());
        store.setAvatar(StringUtils.defaultIfBlank(merchant.getAvatar(), merchant.getLogo()));
        return store;
    }

    private void applyStoreDefaults(MerchantStore store, Merchant merchant) {
        store.setMerchantId(merchant.getId());
        store.setName(StringUtils.defaultIfBlank(store.getName(), merchant.getName()));
        store.setContact(StringUtils.defaultIfBlank(store.getContact(), merchant.getContact()));
        store.setPhone(StringUtils.defaultIfBlank(store.getPhone(), merchant.getPhone()));
        store.setAddress(StringUtils.defaultIfBlank(store.getAddress(), merchant.getAddress()));
        store.setBusinessHours(StringUtils.defaultIfBlank(store.getBusinessHours(), merchant.getBusinessHours()));
        store.setAvatar(StringUtils.defaultIfBlank(store.getAvatar(),
                StringUtils.defaultIfBlank(merchant.getAvatar(), merchant.getLogo())));
        store.setStatus(store.getStatus() == null ? 1 : store.getStatus());
        store.setIsMain(1);
        normalizeNullableStoreFields(store);
    }

    private void normalizeNullableStoreFields(MerchantStore store) {
        store.setName(StringUtils.trimToEmpty(store.getName()));
        store.setContact(StringUtils.trimToEmpty(store.getContact()));
        store.setPhone(StringUtils.trimToEmpty(store.getPhone()));
        store.setAddress(StringUtils.trimToEmpty(store.getAddress()));
        store.setBusinessHours(StringUtils.trimToEmpty(store.getBusinessHours()));
        store.setAvatar(StringUtils.trimToEmpty(store.getAvatar()));
        store.setStatus(store.getStatus() == null ? 1 : store.getStatus());
    }

    private void validateStore(MerchantStore store) {
        if (StringUtils.isBlank(store.getName())) {
            throw new IllegalArgumentException("门店名称不能为空");
        }
        if (store.getName().length() > 100) {
            throw new IllegalArgumentException("门店名称不能超过100个字符");
        }
        if (store.getContact().length() > 50) {
            throw new IllegalArgumentException("联系人不能超过50个字符");
        }
        if (store.getPhone().length() > 20) {
            throw new IllegalArgumentException("联系电话不能超过20个字符");
        }
        if (store.getAddress().length() > 255) {
            throw new IllegalArgumentException("门店地址不能超过255个字符");
        }
        if (store.getBusinessHours().length() > 100) {
            throw new IllegalArgumentException("营业时间不能超过100个字符");
        }
        if (store.getAvatar().length() > 255) {
            throw new IllegalArgumentException("门店图片地址不能超过255个字符");
        }
        if (store.getStatus() != 0 && store.getStatus() != 1) {
            throw new IllegalArgumentException("门店营业状态不合法");
        }
        if (store.getLongitude() != null
                && (store.getLongitude().compareTo(new BigDecimal("-180")) < 0
                || store.getLongitude().compareTo(new BigDecimal("180")) > 0)) {
            throw new IllegalArgumentException("门店经度必须在-180到180之间");
        }
        if (store.getLatitude() != null
                && (store.getLatitude().compareTo(new BigDecimal("-90")) < 0
                || store.getLatitude().compareTo(new BigDecimal("90")) > 0)) {
            throw new IllegalArgumentException("门店纬度必须在-90到90之间");
        }
    }

    private void insertPrimaryStore(Merchant merchant, MerchantStore store) {
        applyStoreDefaults(store, merchant);
        validateStore(store);
        if (merchantStoreMapper.insertMerchantStore(store) <= 0) {
            throw new IllegalStateException("主门店创建失败");
        }
        syncStoreCount(merchant.getId());
    }

    private void syncStoreCount(Long merchantId) {
        Merchant update = new Merchant();
        update.setId(merchantId);
        update.setStoreCount(merchantStoreMapper.countMerchantStoreByMerchantId(merchantId));
        merchantMapper.updateMerchant(update);
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
