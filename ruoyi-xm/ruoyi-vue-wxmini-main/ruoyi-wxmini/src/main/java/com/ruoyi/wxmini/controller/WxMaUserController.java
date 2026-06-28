package com.ruoyi.wxmini.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.binarywang.wx.miniapp.util.WxMaConfigHolder;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.config.WxMaServiceManager;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.user.domain.UserAccountCancelRecord;
import com.ruoyi.mall.user.domain.UserInfo;
import com.ruoyi.mall.user.service.IUserAccountCancelRecordService;
import com.ruoyi.mall.user.service.IUserInfoService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

@RestController
@RequestMapping("/wxmini/user")
public class WxMaUserController {
    private static final Logger log = LoggerFactory.getLogger(WxMaUserController.class);

    @Resource
    private WxMaServiceManager wxMaServiceManager;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private IUserInfoService userInfoService;
    @Resource
    private IUserAccountCancelRecordService cancelRecordService;

    @Value("${wxmini.account.cancel-reregister-delay-days:7}")
    private int cancelReregisterDelayDays;

    @GetMapping("/info")
    public AjaxResult info(String appid, String sessionKey,
                           String signature, String rawData, String encryptedData, String iv) {
        UserInfo storedUserInfo = userInfoService.selectUserInfoByUserId(WxMiniUserContext.getCurrentUserId());
        if (StringUtils.isAnyBlank(appid, sessionKey, encryptedData, iv)) {
            return storedUserInfo == null ? AjaxResult.error("用户不存在") : AjaxResult.success(buildUserInfoResult(storedUserInfo));
        }

        WxMaService maService = getOrLoadService(appid);
        if (maService == null) {
            return AjaxResult.error(String.format("未找到AppID [%s] 的配置", appid));
        }

        try {
            WxMaUserInfo wxUserInfo = maService.getUserService().getUserInfo(sessionKey, encryptedData, iv);
            if (storedUserInfo != null) {
                storedUserInfo.setAvatarUrl(wxUserInfo.getAvatarUrl());
                storedUserInfo.setUserName(wxUserInfo.getNickName());
                userInfoService.updateUserInfo(storedUserInfo);
            }
            return AjaxResult.success(wxUserInfo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error();
        } finally {
            WxMaConfigHolder.remove();
        }
    }

    private Map<String, Object> buildUserInfoResult(UserInfo userInfo) {
        Map<String, Object> result = new HashMap<>();
        result.put("openId", userInfo.getOpenId());
        result.put("userId", userInfo.getUserId());
        result.put("userName", userInfo.getUserName());
        result.put("userType", "0");
        result.put("phone", userInfo.getPhone() != null ? userInfo.getPhone() : "");
        result.put("avatarUrl", userInfo.getAvatarUrl());
        result.put("apiToken", "");
        return result;
    }

    @PutMapping("/info")
    public AjaxResult updateInfo(@RequestBody Map<String, String> body) {
        String userId = WxMiniUserContext.getCurrentUserId();
        UserInfo userInfo = userInfoService.selectUserInfoByUserId(userId);
        if (userInfo == null) {
            return AjaxResult.error("用户不存在");
        }
        if (body.containsKey("userName")) {
            userInfo.setUserName(body.get("userName"));
        }
        if (body.containsKey("avatarUrl")) {
            userInfo.setAvatarUrl(body.get("avatarUrl"));
        }
        userInfoService.updateUserInfo(userInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("openId", userInfo.getOpenId());
        result.put("userId", userInfo.getUserId());
        result.put("userName", userInfo.getUserName());
        result.put("userType", "0");
        result.put("phone", userInfo.getPhone() != null ? userInfo.getPhone() : "");
        result.put("avatarUrl", userInfo.getAvatarUrl());
        result.put("apiToken", "");
        return AjaxResult.success(result);
    }

    @DeleteMapping("/account")
    public AjaxResult cancelAccount(@RequestHeader(value = "X-Wx-AppId", required = false) String appId) {
        String userId = WxMiniUserContext.getCurrentUserId();
        UserInfo userInfo = userInfoService.selectUserInfoByUserId(userId);
        if (userInfo == null) {
            return AjaxResult.error("用户不存在");
        }
        String openIdHash = cancelRecordService.hashOpenId(appId, userInfo.getOpenId());
        if (StringUtils.isNotBlank(openIdHash)) {
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.DAY_OF_YEAR, Math.max(cancelReregisterDelayDays, 0));

            UserAccountCancelRecord record = new UserAccountCancelRecord();
            record.setAppId(appId);
            record.setOpenIdHash(openIdHash);
            record.setUserId(userInfo.getUserId());
            record.setCancelTime(now);
            record.setAllowRegisterTime(calendar.getTime());
            cancelRecordService.saveCancelRecord(record);
        }

        userInfo.setUserName("已注销用户");
        userInfo.setPhone("");
        userInfo.setAvatarUrl("");
        userInfo.setOpenId("");
        userInfo.setUnionId("");
        userInfo.setDelFlag("2");
        userInfoService.updateUserInfo(userInfo);
        return AjaxResult.success("账号已注销");
    }

    @PostMapping("/phone/bind")
    public AjaxResult bindPhone(@RequestBody Map<String, String> body,
                                @RequestHeader(value = "X-Wx-AppId", required = false) String headerAppId) {
        String code = body != null ? body.get("code") : null;
        String bodyAppId = body != null ? body.get("appid") : null;
        String appid = StringUtils.defaultIfBlank(bodyAppId, headerAppId);
        if (StringUtils.isBlank(code)) {
            return AjaxResult.error("code不能为空");
        }

        WxMaService maService = getOrLoadService(appid);
        if (maService == null) {
            return AjaxResult.error(String.format("未找到AppID [%s] 的配置", appid));
        }

        try {
            String accessToken = maService.getAccessToken();
            String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken;
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            Map<String, String> reqBody = new HashMap<>();
            reqBody.put("code", code);
            String response = restTemplate.postForObject(url, reqBody, String.class);

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode json = objectMapper.readTree(response);
            if (json.path("errcode").asInt() != 0) {
                return AjaxResult.error("手机号获取失败: " + json.path("errmsg").asText());
            }
            String phone = json.path("phone_info").path("phoneNumber").asText();

            String userId = WxMiniUserContext.getCurrentUserId();
            UserInfo userInfo = userInfoService.selectUserInfoByUserId(userId);
            if (userInfo != null) {
                userInfo.setPhone(phone);
                userInfoService.updateUserInfo(userInfo);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("phone", phone);
            result.put("userName", userInfo != null ? userInfo.getUserName() : "微信用户");
            result.put("avatarUrl", userInfo != null ? userInfo.getAvatarUrl() : "");
            return AjaxResult.success(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error("手机号获取失败: " + e.getMessage());
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
        if (StringUtils.isBlank(appId)) {
            return null;
        }
        Merchant merchant = merchantService.selectMerchantByCAppId(appId);
        if (merchant != null && StringUtils.isNotBlank(merchant.getCMiniAppSecret())) {
            wxMaServiceManager.registerOrRefresh(appId, merchant.getCMiniAppSecret());
            return wxMaServiceManager.getService(appId);
        }
        return null;
    }
}
