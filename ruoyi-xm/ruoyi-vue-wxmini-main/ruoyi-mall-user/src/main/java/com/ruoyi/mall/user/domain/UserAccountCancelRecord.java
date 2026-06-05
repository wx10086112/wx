package com.ruoyi.mall.user.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 小程序用户注销后重新注册限制记录。
 */
public class UserAccountCancelRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String appId;
    private String openIdHash;
    private String userId;
    private Date cancelTime;
    private Date allowRegisterTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getOpenIdHash() {
        return openIdHash;
    }

    public void setOpenIdHash(String openIdHash) {
        this.openIdHash = openIdHash;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(Date cancelTime) {
        this.cancelTime = cancelTime;
    }

    public Date getAllowRegisterTime() {
        return allowRegisterTime;
    }

    public void setAllowRegisterTime(Date allowRegisterTime) {
        this.allowRegisterTime = allowRegisterTime;
    }
}
