package com.ruoyi.wxmini.bo;

import java.util.ArrayList;
import java.util.List;

public class WxMiniAuthContext {

    public static final String USER_TYPE_WX_USER = "WX_USER";

    public static final String USER_TYPE_MERCHANT_STAFF = "MERCHANT_STAFF";

    private String userId;

    private String userType;

    private Long staffId;

    private Long merchantId;

    private Long storeId;

    private List<String> roleCodes = new ArrayList<>();

    private List<String> permissionCodes = new ArrayList<>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes == null ? new ArrayList<>() : new ArrayList<>(roleCodes);
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes == null ? new ArrayList<>() : new ArrayList<>(permissionCodes);
    }
}
