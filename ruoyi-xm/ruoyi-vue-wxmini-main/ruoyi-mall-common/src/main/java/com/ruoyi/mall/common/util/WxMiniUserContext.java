package com.ruoyi.mall.common.util;

import com.ruoyi.mall.common.bo.WxMiniAuthContext;

import java.util.Collections;
import java.util.List;

/**
 * 微信小程序用户上下文处理器
 *
 * @author weijiayu
 * @date 2025/4/22 23:57
 */
public class WxMiniUserContext {

    private static final ThreadLocal<WxMiniAuthContext> currentContext = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(String userId) {
        WxMiniAuthContext authContext = new WxMiniAuthContext();
        authContext.setUserId(userId);
        authContext.setUserType(WxMiniAuthContext.USER_TYPE_WX_USER);
        currentContext.set(authContext);
    }

    public static void setCurrentUserContext(WxMiniAuthContext authContext) {
        if (authContext == null) {
            currentContext.remove();
            return;
        }
        currentContext.set(authContext);
    }

    public static WxMiniAuthContext getCurrentUserContext() {
        return currentContext.get();
    }

    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        WxMiniAuthContext authContext = currentContext.get();
        return authContext == null ? null : authContext.getUserId();
    }

    public static String getCurrentUserType() {
        WxMiniAuthContext authContext = currentContext.get();
        return authContext == null ? null : authContext.getUserType();
    }

    public static Long getCurrentStaffId() {
        WxMiniAuthContext authContext = currentContext.get();
        return authContext == null ? null : authContext.getStaffId();
    }

    public static Long getCurrentMerchantId() {
        WxMiniAuthContext authContext = currentContext.get();
        return authContext == null ? null : authContext.getMerchantId();
    }

    public static Long getCurrentStoreId() {
        WxMiniAuthContext authContext = currentContext.get();
        return authContext == null ? null : authContext.getStoreId();
    }

    public static List<String> getCurrentRoleCodes() {
        WxMiniAuthContext authContext = currentContext.get();
        if (authContext == null || authContext.getRoleCodes() == null) {
            return Collections.emptyList();
        }
        return authContext.getRoleCodes();
    }

    public static List<String> getCurrentPermissionCodes() {
        WxMiniAuthContext authContext = currentContext.get();
        if (authContext == null || authContext.getPermissionCodes() == null) {
            return Collections.emptyList();
        }
        return authContext.getPermissionCodes();
    }

    public static boolean isMerchantStaff() {
        return WxMiniAuthContext.USER_TYPE_MERCHANT_STAFF.equals(getCurrentUserType());
    }

    public static boolean isWxUser() {
        return WxMiniAuthContext.USER_TYPE_WX_USER.equals(getCurrentUserType());
    }

    public static boolean hasPermission(String permissionCode) {
        return getCurrentPermissionCodes().contains(permissionCode);
    }

    public static boolean hasAnyPermission(String... permissionCodes) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return true;
        }
        for (String permissionCode : permissionCodes) {
            if (hasPermission(permissionCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清除当前用户 ID
     */
    public static void clear() {
        currentContext.remove();
    }
}
