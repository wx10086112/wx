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
    private final Map<String, String> secretMap = new ConcurrentHashMap<>();

    /**
     * 注册一个AppID对应的WxMaService
     */
    public void register(String appId, String secret) {
        registerOrRefresh(appId, secret);
    }

    /**
     * 新增或刷新一个AppID对应的WxMaService。
     * 后台修改Secret后，运行中的缓存也能立即生效。
     */
    public void registerOrRefresh(String appId, String secret) {
        String normalizedAppId = normalize(appId);
        String normalizedSecret = normalize(secret);
        if (normalizedAppId == null || normalizedSecret == null) {
            return;
        }

        WxMaService existing = serviceMap.get(normalizedAppId);
        String existingSecret = secretMap.get(normalizedAppId);
        if (normalizedSecret.equals(existingSecret)) {
            return;
        }

        serviceMap.put(normalizedAppId, buildService(normalizedAppId, normalizedSecret));
        secretMap.put(normalizedAppId, normalizedSecret);
        if (existing == null) {
            log.info("注册微信小程序配置: appId={}", normalizedAppId);
        } else {
            log.info("刷新微信小程序配置: appId={}", normalizedAppId);
        }
    }

    /**
     * 获取WxMaService，找不到返回null
     */
    public WxMaService getService(String appId) {
        String normalizedAppId = normalize(appId);
        if (normalizedAppId == null) {
            return null;
        }
        return serviceMap.get(normalizedAppId);
    }

    /**
     * 检查AppID是否已注册
     */
    public boolean hasService(String appId) {
        String normalizedAppId = normalize(appId);
        return normalizedAppId != null && serviceMap.containsKey(normalizedAppId);
    }

    /**
     * 移除某个AppID的配置
     */
    public void remove(String appId) {
        String normalizedAppId = normalize(appId);
        if (normalizedAppId != null) {
            serviceMap.remove(normalizedAppId);
            secretMap.remove(normalizedAppId);
        }
    }

    private WxMaService buildService(String appId, String secret) {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(appId);
        config.setSecret(secret);

        WxMaServiceImpl service = new WxMaServiceImpl();
        service.setMultiConfigs(Collections.singletonMap(appId, config));
        return service;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
