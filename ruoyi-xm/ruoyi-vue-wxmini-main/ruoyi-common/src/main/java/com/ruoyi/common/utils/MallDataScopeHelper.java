package com.ruoyi.common.utils;

import com.ruoyi.common.core.domain.model.LoginUser;

/**
 * 商城后台数据权限工具类
 */
public class MallDataScopeHelper {

    public static boolean isSuperAdmin() {
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            return loginUser != null && SecurityUtils.isAdmin(loginUser.getUserId());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDistributorAdmin() {
        return "DISTRIBUTOR".equals(SecurityUtils.getAccountType());
    }

    public static boolean isMerchantAdmin() {
        return "MERCHANT".equals(SecurityUtils.getAccountType());
    }

    public static Long currentDistributorId() {
        return SecurityUtils.getDistributorId();
    }

    public static Long currentMerchantId() {
        return SecurityUtils.getMerchantId();
    }

    /**
     * 超管是否处于平台总视角（未切换到任何分销商视角）
     */
    public static boolean isSuperAdminPlatformView() {
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser == null || !SecurityUtils.isAdmin(loginUser.getUserId())) {
                return false;
            }
            String avt = loginUser.getActiveViewType();
            return avt == null || "PLATFORM".equals(avt);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 超管是否处于某分销商视角
     */
    public static boolean isSuperAdminDistributorView() {
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser == null || !SecurityUtils.isAdmin(loginUser.getUserId())) {
                return false;
            }
            return "DISTRIBUTOR".equals(loginUser.getActiveViewType());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取当前有效的分销商ID
     * - 超管平台视角：null（不限制）
     * - 超管分销商视角：activeDistributorId
     * - 分销商本人：loginUser.distributorId
     * - 其他：null
     */
    public static Long currentEffectiveDistributorId() {
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser == null) {
                return null;
            }
            if (SecurityUtils.isAdmin(loginUser.getUserId())) {
                if ("DISTRIBUTOR".equals(loginUser.getActiveViewType())) {
                    return loginUser.getActiveDistributorId();
                }
                return null;
            }
            return loginUser.getDistributorId();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前有效的商家ID
     * - 商家账号：loginUser.merchantId
     * - 其他：null
     */
    public static Long currentEffectiveMerchantId() {
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser == null) {
                return null;
            }
            if (SecurityUtils.isAdmin(loginUser.getUserId())) {
                return null;
            }
            return loginUser.getMerchantId();
        } catch (Exception e) {
            return null;
        }
    }
}
