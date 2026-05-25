package com.ruoyi.mall.user.bo;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.ruoyi.mall.user.domain.UserInfo;
import org.apache.commons.lang3.StringUtils;

/**
 * @author weijiayu
 * @date 2025/4/25 21:38
 */
public class WxUserInfo {

    private String sessionKey;
    private String openId;

    private String userName;
    /**
     * 用户类型。在小程序端可以根据用户类型做页面权限访问控制
     */
    private String userType;
    private String phone;
    private String avatarUrl;

    private String apiToken;

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public void wapper(WxMaJscode2SessionResult wxSession, UserInfo userInfo) {
        this.sessionKey = wxSession.getSessionKey();
        this.openId = wxSession.getOpenid();

        this.userName = userInfo.getUserName();
        this.userType = userInfo.getUserType();
        this.phone = userInfo.getPhone();
        this.avatarUrl = userInfo.getAvatarUrl();

        if (StringUtils.isEmpty(this.userName)) {
            this.userName = "微信用户";
        }
        if (StringUtils.isEmpty(this.userType)) {
            this.userType = "0";
        }
    }
}
