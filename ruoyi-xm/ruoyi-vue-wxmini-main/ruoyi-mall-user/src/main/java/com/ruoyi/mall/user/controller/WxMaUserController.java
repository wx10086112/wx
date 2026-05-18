package com.ruoyi.mall.user.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.binarywang.wx.miniapp.util.WxMaConfigHolder;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.user.domain.UserInfo;
import com.ruoyi.mall.user.service.IUserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 微信小程序用户接口
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@RestController
@RequestMapping("/wxmini/user")
public class WxMaUserController {
    private static final Logger log = LoggerFactory.getLogger(WxMaUserController.class);

    private final WxMaService wxMaService;
    @Resource
    private IUserInfoService userInfoService;

    public WxMaUserController(WxMaService wxMaService) {
        this.wxMaService = wxMaService;
    }

    /**
     * 获取用户信息接口
     */
    @GetMapping("/info")
    public AjaxResult info(String appid, String sessionKey,
                           String signature, String rawData, String encryptedData, String iv) {
        if (!wxMaService.switchover(appid)) {
            return AjaxResult.error(String.format("can not find appid=[%s] config", appid));
        }

        try {
            WxMaUserInfo wxUserInfo = wxMaService.getUserService().getUserInfo(sessionKey, encryptedData, iv);
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

    /**
     * 获取用户绑定手机号信息
     */
    @GetMapping("/phone")
    public AjaxResult phone(String appid, String sessionKey, String signature,
                            String rawData, String encryptedData, String iv) {
        if (!wxMaService.switchover(appid)) {
            return AjaxResult.error(String.format("can not find appid=[%s] config", appid));
        }

        try {
            WxMaPhoneNumberInfo phoneNoInfo = wxMaService.getUserService().getPhoneNoInfo(sessionKey, encryptedData,
                    iv);
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

}
