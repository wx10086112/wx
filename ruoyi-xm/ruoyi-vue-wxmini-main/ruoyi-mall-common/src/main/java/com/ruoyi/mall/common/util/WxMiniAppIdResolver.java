package com.ruoyi.mall.common.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 微信小程序 AppID 解析工具
 * 优先从 Header 读取，其次从 query 参数读取
 */
public class WxMiniAppIdResolver {

    /**
     * 解析 C 端小程序 AppID
     * Header: X-Wx-AppId > Query: appid
     */
    public static String resolveCAppId(HttpServletRequest request) {
        String appId = request.getHeader("X-Wx-AppId");
        if (StringUtils.isBlank(appId)) {
            appId = request.getParameter("appid");
        }
        return appId;
    }

    /**
     * 解析商家端小程序 AppID
     * Header: X-Merchant-AppId > Query: appid
     */
    public static String resolveMerchantAppId(HttpServletRequest request) {
        String appId = request.getHeader("X-Merchant-AppId");
        if (StringUtils.isBlank(appId)) {
            appId = request.getParameter("appid");
        }
        return appId;
    }
}
