package com.ruoyi.mall.common.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多租户微信小程序SDK管理器
 * 每个商家的AppID独立注册，按AppId获取对应的WxMaService
 */
@Component
public class WxMaServiceManager {

    private static final Logger log = LoggerFactory.getLogger(WxMaServiceManager.class);

    private final Map<String, WxMaService> serviceMap = new ConcurrentHashMap<>();

    /**
     * 注册一个AppID对应的WxMaService
     */
    public void register(String appId, String secret) {
        if (appId == null || secret == null) return;
        if (serviceMap.containsKey(appId)) return;

        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(appId);
        config.setSecret(secret);

        WxMaServiceImpl service = new WxMaServiceImpl();
        service.setMultiConfigs(Collections.singletonMap(appId, config));

        serviceMap.put(appId, service);
        log.info("注册微信小程序配置: appId={}", appId);
    }

    /**
     * 获取WxMaService，找不到返回null
     */
    public WxMaService getService(String appId) {
        if (appId == null) return null;
        return serviceMap.get(appId);
    }

    /**
     * 检查AppID是否已注册
     */
    public boolean hasService(String appId) {
        return appId != null && serviceMap.containsKey(appId);
    }

    /**
     * 移除某个AppID的配置
     */
    public void remove(String appId) {
        if (appId != null) {
            serviceMap.remove(appId);
        }
    }
}
