package com.ruoyi.mall.user.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.util.WxMaConfigHolder;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.mall.common.config.WxMaServiceManager;
import com.ruoyi.mall.common.service.IWxMiniJwtService;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.mapper.MerchantMapper;
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

/**
 * 微信小程序登录接口（多租户：按AppId查商家配置）
 */
@RestController
@RequestMapping("/wxmini")
public class WxLoginController {
    private static final Logger log = LoggerFactory.getLogger(WxLoginController.class);

    @Resource
    private WxMaServiceManager wxMaServiceManager;
    @Resource
    private MerchantMapper merchantMapper;
    @Resource
    private IUserInfoService userInfoService;
    @Resource
    private IWxMiniJwtService jwtService;

    @GetMapping("/login")
    public AjaxResult login(String appid, String code) {
        if (StringUtils.isEmpty(code)) {
            return AjaxResult.error("empty jscode");
        }
        if (StringUtils.isEmpty(appid)) {
            return AjaxResult.error("empty appid");
        }

        // 如果缓存中没有，从数据库加载
        WxMaService maService = wxMaServiceManager.getService(appid);
        if (maService == null) {
            Merchant merchant = merchantMapper.selectMerchantByCAppId(appid);
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
