package com.ruoyi.mall.common.config;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class WxPayServiceConfiguration {

    @Bean
    public WxPayService wxPayService(Environment env) {
        WxPayConfig payConfig = new WxPayConfig();
        payConfig.setAppId(trim(env.getProperty("wx.pay.appId")));
        payConfig.setMchId(trim(env.getProperty("wx.pay.mchId")));
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

        WxPayServiceImpl wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(payConfig);
        return wxPayService;
    }

    private String trim(String value) {
        return StringUtils.trimToNull(value);
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.isNotBlank(first) ? first : second;
    }
}
