package com.ruoyi.mall.merchant.service;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.merchant.domain.MerchantUser;
import com.ruoyi.mall.merchant.mapper.MerchantUserMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 商家员工手机号校验：同一商家下手机号不允许重复。
 *
 * <p>商家端支持用手机号登录，手机号重复会导致登录时无法确定账号，因此在新增/修改员工、
 * 自动创建 owner 账号时统一走这里校验。</p>
 */
public final class MerchantUserPhoneValidator {

    private MerchantUserPhoneValidator() {
    }

    /**
     * 校验手机号在同一商家下是否唯一。
     *
     * @param mapper     商家用户 Mapper
     * @param merchantId 商家ID
     * @param phone      待校验手机号，为空时直接放行
     * @param excludeId  需要排除的用户ID（修改场景传当前用户ID），新增传 null
     */
    public static void check(MerchantUserMapper mapper, Long merchantId, String phone, Long excludeId) {
        String normalizedPhone = StringUtils.trimToNull(phone);
        if (normalizedPhone == null) {
            return;
        }
        if (merchantId == null) {
            throw new ServiceException("商家ID不能为空");
        }
        MerchantUser query = new MerchantUser();
        query.setMerchantId(merchantId);
        query.setPhone(normalizedPhone);
        List<MerchantUser> users = mapper.selectMerchantUserList(query);
        for (MerchantUser user : users) {
            if (excludeId != null && excludeId.equals(user.getId())) {
                continue;
            }
            throw new ServiceException("当前商家已存在使用该手机号的账号");
        }
    }
}
