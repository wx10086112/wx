package com.ruoyi.mall.user.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.binarywang.wx.miniapp.util.WxMaConfigHolder;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.config.WxMaServiceManager;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.mapper.MerchantMapper;
import com.ruoyi.mall.user.domain.UserInfo;
import com.ruoyi.mall.user.service.IUserInfoService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 微信小程序用户接口（多租户）
 */
@RestController
@RequestMapping("/wxmini/user")
public class WxMaUserController {
    private static final Logger log = LoggerFactory.getLogger(WxMaUserController.class);

    @Resource
    private WxMaServiceManager wxMaServiceManager;
    @Resource
    private MerchantMapper merchantMapper;
    @Resource
    private IUserInfoService userInfoService;

    @GetMapping("/info")
    public AjaxResult info(String appid, String sessionKey,
                           String signature, String rawData, String encryptedData, String iv) {
        WxMaService maService = getOrLoadService(appid);
        if (maService == null) {
            return AjaxResult.error(String.format("未找到AppID [%s] 的配置", appid));
        }

        try {
            WxMaUserInfo wxUserInfo = maService.getUserService().getUserInfo(sessionKey, encryptedData, iv);
            UserInfo userInfo = userInfoService.selectUserInfoByUserId(WxMiniUserContext.getCurrentUserId());
            if (userInfo != null) {
                userInfo.setAvatarUrl(wxUserInfo.getAvatarUrl());
                userInfo.setUserName(wxUserInfo.getNickName());
                userInfoService.updateUserInfo(userInfo);
            }
            return AjaxResult.success(wxUserInfo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        } finally {
            WxMaConfigHolder.remove();
        }
    }

    @GetMapping("/phone")
    public AjaxResult phone(String appid, String sessionKey, String signature,
                            String rawData, String encryptedData, String iv) {
        WxMaService maService = getOrLoadService(appid);
        if (maService == null) {
            return AjaxResult.error(String.format("未找到AppID [%s] 的配置", appid));
        }

        try {
            WxMaPhoneNumberInfo phoneNoInfo = maService.getUserService().getPhoneNoInfo(sessionKey, encryptedData, iv);
            String phone = phoneNoInfo.getPhoneNumber();
            UserInfo userInfo = userInfoService.selectUserInfoByUserId(WxMiniUserContext.getCurrentUserId());
            if (userInfo != null) {
                userInfo.setPhone(phone);
                userInfoService.updateUserInfo(userInfo);
            }
            return AjaxResult.success(phoneNoInfo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        } finally {
            WxMaConfigHolder.remove();
        }
    }

    private WxMaService getOrLoadService(String appId) {
        WxMaService service = wxMaServiceManager.getService(appId);
        if (service != null) return service;
        Merchant merchant = merchantMapper.selectMerchantByCAppId(appId);
        if (merchant != null && StringUtils.isNotBlank(merchant.getCMiniAppSecret())) {
            wxMaServiceManager.register(appId, merchant.getCMiniAppSecret());
            return wxMaServiceManager.getService(appId);
        }
        return null;
    }
}
