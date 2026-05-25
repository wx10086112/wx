package com.ruoyi.wxmini.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaMessage;
import cn.binarywang.wx.miniapp.constant.WxMaConstants;
import cn.binarywang.wx.miniapp.message.WxMaMessageRouter;
import cn.binarywang.wx.miniapp.util.WxMaConfigHolder;
import com.ruoyi.mall.common.config.WxMaServiceManager;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 微信消息推送验证（多租户：URL路径带AppId）
 * 每个商家的verifyUrl: https://yourdomain.com/wxmini/portal/{c_mini_app_id}
 */
@RestController
@RequestMapping("/wxmini/portal/{appid}")
public class WxPortalController {
    private static final Logger log = LoggerFactory.getLogger(WxPortalController.class);

    @Resource
    private WxMaServiceManager wxMaServiceManager;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private WxMaMessageRouter wxMaMessageRouter;

    @GetMapping(produces = "text/plain;charset=utf-8")
    public String authGet(@PathVariable String appid,
                          @RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echostr) {
        log.info("接收到来自微信服务器的认证消息：appid=[{}], signature=[{}], timestamp=[{}], nonce=[{}], echostr=[{}]",
                appid, signature, timestamp, nonce, echostr);

        if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
            throw new IllegalArgumentException("请求参数非法，请核实!");
        }

        WxMaService maService = getOrLoadService(appid);
        if (maService == null) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appid));
        }

        try {
            if (maService.checkSignature(timestamp, nonce, signature)) {
                return echostr;
            }
            return "非法请求";
        } finally {
            WxMaConfigHolder.remove();
        }
    }

    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(@PathVariable String appid,
                       @RequestBody String requestBody,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature,
                       @RequestParam(name = "encrypt_type", required = false) String encryptType,
                       @RequestParam(name = "signature", required = false) String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce) {
        log.info("接收微信请求：appid=[{}], msg_signature=[{}], encrypt_type=[{}], signature=[{}], timestamp=[{}], nonce=[{}]",
                appid, msgSignature, encryptType, signature, timestamp, nonce);

        WxMaService maService = getOrLoadService(appid);
        if (maService == null) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置", appid));
        }

        try {
            final boolean isJson = Objects.equals(maService.getWxMaConfig().getMsgDataFormat(),
                    WxMaConstants.MsgDataFormat.JSON);
            if (StringUtils.isBlank(encryptType)) {
                WxMaMessage inMessage = isJson ? WxMaMessage.fromJson(requestBody) : WxMaMessage.fromXml(requestBody);
                this.route(inMessage);
                return "success";
            }

            if ("aes".equals(encryptType)) {
                WxMaMessage inMessage;
                if (isJson) {
                    inMessage = WxMaMessage.fromEncryptedJson(requestBody, maService.getWxMaConfig());
                } else {
                    inMessage = WxMaMessage.fromEncryptedXml(requestBody, maService.getWxMaConfig(),
                            timestamp, nonce, msgSignature);
                }
                this.route(inMessage);
                return "success";
            }
            throw new RuntimeException("不可识别的加密类型：" + encryptType);
        } finally {
            WxMaConfigHolder.remove();
        }
    }

    private WxMaService getOrLoadService(String appId) {
        WxMaService service = wxMaServiceManager.getService(appId);
        if (service != null) return service;
        Merchant merchant = merchantService.selectMerchantByCAppId(appId);
        if (merchant != null && StringUtils.isNotBlank(merchant.getCMiniAppSecret())) {
            wxMaServiceManager.register(appId, merchant.getCMiniAppSecret());
            return wxMaServiceManager.getService(appId);
        }
        return null;
    }

    private void route(WxMaMessage message) {
        try {
            wxMaMessageRouter.route(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
