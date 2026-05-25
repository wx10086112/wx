package com.ruoyi.wxmini.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.mall.common.config.WxMaServiceManager;
import com.ruoyi.mall.common.service.IWxMiniJwtService;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.user.bo.WxUserInfo;
import com.ruoyi.mall.user.domain.UserInfo;
import com.ruoyi.mall.user.service.IUserInfoService;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/wxmini")
public class WxLoginController {
    private static final Logger log = LoggerFactory.getLogger(WxLoginController.class);

    @Resource
    private WxMaServiceManager wxMaServiceManager;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private IUserInfoService userInfoService;
    @Resource
    private IWxMiniJwtService jwtService;

    @GetMapping("/login/test")
    public AjaxResult testLogin(String appid) {
        String testOpenId = "test_openid_001";
        UserInfo userInfo = userInfoService.selectUserInfoByOpenId(testOpenId);
        if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setUserId(UUID.randomUUID().toString());
            userInfo.setOpenId(testOpenId);
            userInfo.setUserName("测试用户");
            userInfo.setUserType("0");
            userInfo.setPhone("13800001111");
            userInfo.setAvatarUrl("");
            userInfoService.insertUserInfo(userInfo);
        }

        WxUserInfo wxUserInfo = new WxUserInfo();
        wxUserInfo.setOpenId(userInfo.getOpenId());
        wxUserInfo.setUserName(userInfo.getUserName() != null ? userInfo.getUserName() : "测试用户");
        wxUserInfo.setUserType("0");
        wxUserInfo.setPhone(userInfo.getPhone() != null ? userInfo.getPhone() : "");
        wxUserInfo.setAvatarUrl(userInfo.getAvatarUrl() != null ? userInfo.getAvatarUrl() : "");
        wxUserInfo.setApiToken(jwtService.createToken(userInfo.getUserId()));
        return AjaxResult.success(wxUserInfo);
    }

    @GetMapping("/login")
    public AjaxResult login(String appid, String code) {
        if (StringUtils.isEmpty(code)) {
            return AjaxResult.error("empty jscode");
        }
        if (StringUtils.isEmpty(appid)) {
            return AjaxResult.error("empty appid");
        }

        WxMaService maService = wxMaServiceManager.getService(appid);
        if (maService == null) {
            Merchant merchant = merchantService.selectMerchantByCAppId(appid);
            if (merchant == null || StringUtils.isBlank(merchant.getCMiniAppSecret())) {
                return AjaxResult.error(String.format("未找到AppID [%s] 对应的商家配置", appid));
            }
            wxMaServiceManager.register(appid, merchant.getCMiniAppSecret());
            maService = wxMaServiceManager.getService(appid);
        }

        if (maService == null) {
            return AjaxResult.error("微信服务初始化失败");
        }

        WxUserInfo wxUserInfo = new WxUserInfo();
        try {
            WxMaJscode2SessionResult session = maService.getUserService().getSessionInfo(code);
            String openId = session.getOpenid();
            UserInfo userInfo = userInfoService.selectUserInfoByOpenId(openId);
            if (userInfo == null) {
                userInfo = new UserInfo();
                userInfo.setUserId(UUID.randomUUID().toString());
                userInfo.setOpenId(openId);
                userInfo.setUnionId(session.getUnionid());
                userInfoService.insertUserInfo(userInfo);
            }
            wxUserInfo.wapper(session, userInfo);
            wxUserInfo.setApiToken(jwtService.createToken(userInfo.getUserId()));
            return AjaxResult.success(wxUserInfo);
        } catch (WxErrorException e) {
            log.error("微信登录失败: appId={}, error={}", appid, e.getMessage(), e);
            return AjaxResult.error("微信登录失败");
        }
    }
}
