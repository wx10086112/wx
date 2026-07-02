package com.ruoyi.mall.common.config;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class WxPayServiceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(WxPayServiceConfiguration.class);
    private static final String SERVICE_PROVIDER_APP_ID_PLACEHOLDER_PREFIX = "SP_MCH_";

    @Bean
    public WxPayService wxPayService(Environment env) {
        WxPayServiceImpl wxPayService = new WxPayServiceImpl();
        String appId = trim(env.getProperty("wx.pay.appId"));
        String mchId = trim(env.getProperty("wx.pay.mchId"));
        if (StringUtils.isBlank(mchId)) {
            log.warn("WeChat Pay service provider mchId is blank; WxPayService is not configured.");
            return wxPayService;
        }
        if (StringUtils.isBlank(appId)) {
            appId = syntheticServiceProviderAppId(mchId);
            log.warn("wx.pay.appId is blank; using an internal placeholder for WxPayService. "
                    + "It will not be sent as sp_appid. wx.pay.mchId={}", mask(mchId));
        }

        WxPayConfig payConfig = new WxPayConfig();
        payConfig.setAppId(appId);
        payConfig.setMchId(mchId);
        payConfig.setMchKey(trim(env.getProperty("wx.pay.mchKey")));
        payConfig.setSubAppId(trim(env.getProperty("wx.pay.subAppId")));
        payConfig.setSubMchId(trim(env.getProperty("wx.pay.subMchId")));
        payConfig.setKeyPath(trim(env.getProperty("wx.pay.keyPath")));
        payConfig.setServiceId(trim(env.getProperty("wx.pay.serviceId")));
        payConfig.setPayScoreNotifyUrl(trim(env.getProperty("wx.pay.payScoreNotifyUrl")));
        payConfig.setPrivateKeyPath(trim(env.getProperty("wx.pay.privateKeyPath")));
        payConfig.setPrivateCertPath(trim(env.getProperty("wx.pay.privateCertPath")));
        payConfig.setCertSerialNo(trim(env.getProperty("wx.pay.certSerialNo")));
        payConfig.setApiV3Key(trim(firstNonBlank(env.getProperty("wx.pay.apiV3Key"), env.getProperty("wx.pay.apiv3Key"))));
        payConfig.setPublicKeyId(trim(env.getProperty("wx.pay.publicKeyId")));
        payConfig.setPublicKeyPath(trim(env.getProperty("wx.pay.publicKeyPath")));
        payConfig.setUseSandboxEnv(Boolean.parseBoolean(firstNonBlank(env.getProperty("wx.pay.useSandboxEnv"), "false")));

        wxPayService.setConfig(payConfig);
        return wxPayService;
    }

    public static String syntheticServiceProviderAppId(String mchId) {
        return SERVICE_PROVIDER_APP_ID_PLACEHOLDER_PREFIX + StringUtils.trimToEmpty(mchId);
    }

    public static boolean isSyntheticServiceProviderAppId(String appId, String mchId) {
        return StringUtils.isNotBlank(mchId)
                && StringUtils.equals(appId, syntheticServiceProviderAppId(mchId));
    }

    private String trim(String value) {
        return StringUtils.trimToNull(value);
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.isNotBlank(first) ? first : second;
    }

    private String mask(String value) {
        if (StringUtils.isBlank(value)) {
            return "<empty>";
        }
        String trimmed = value.trim();
        return trimmed.length() <= 4 ? "****" : trimmed.substring(0, 2) + "****" + trimmed.substring(trimmed.length() - 2);
    }
}
