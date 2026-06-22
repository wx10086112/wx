package com.ruoyi.mall.common.service;

import com.github.binarywang.wxpay.bean.request.WxPayPartnerUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.mall.common.bo.WxPayCreateOrderParam;
import com.ruoyi.mall.common.vo.WxPayParamVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
public abstract class AbsWxPayBaseService<P> {

    @Resource
    private WxPayService wxPayService;

    @Value("${wx.pay.notify-url}")
    private String wxPayNotifyUrl;

    private final ConcurrentHashMap<String, Object> resourceFlagMap = new ConcurrentHashMap<>();

    public WxPayParamVo createOrder(String userId, P payVo) throws Exception {
        String resourceId = this.getResourceId(payVo);
        if (resourceFlagMap.putIfAbsent(resourceId, Boolean.TRUE) != null) {
            throw new RuntimeException("请稍后再试");
        }
        try {
            if (!this.checkBeforeCreatOrder(userId, payVo)) {
                return null;
            }
            HashMap<String, Object> contextMap = new HashMap<>();
            WxPayCreateOrderParam orderParam = this.buildOrderParam(userId, payVo, contextMap);
            if (orderParam == null) {
                return null;
            }
            WxPayUnifiedOrderV3Result.JsapiResult jsapiResult = this.createOrder(orderParam);
            if (jsapiResult == null) {
                return null;
            }
            String orderNo = orderParam.getOrderNo();
            if (this.saveOrderInfo(orderNo, payVo, orderParam, contextMap)) {
                WxPayParamVo payParamVo = new WxPayParamVo();
                payParamVo.setOrderNo(orderNo);
                payParamVo.setPayParam(jsapiResult);
                return payParamVo;
            }
            return null;
        } finally {
            resourceFlagMap.remove(resourceId);
        }
    }

    public WxPayParamVo reCreatOrder(String userId, String orderNo) throws Exception {
        if (!this.checkUserOrderIsMatch(userId, orderNo)) {
            return null;
        }
        P payVo = this.buildPayVoWithReCreatOrder(userId, orderNo);
        if (this.queryPayResultAndUpdOrderStatus(orderNo)) {
            return null;
        }
        return this.createOrder(userId, payVo);
    }

    public Boolean cancelOrder(String userId, String orderNo) throws WxPayException {
        if (!this.checkUserOrderIsMatch(userId, orderNo)) {
            return false;
        }
        this.queryPayResultAndUpdOrderStatus(orderNo);
        return true;
    }

    public Boolean queryPayResultAndUpdOrderStatus(String orderNo) throws WxPayException {
        throw new UnsupportedOperationException("微信支付查询/关单请在业务服务中实现");
    }

    public Boolean handlePayResult(Boolean payResult, String orderNo) {
        if (payResult) {
            return this.updOrderWithPaySuccess(orderNo);
        }
        return this.closeOrder(orderNo);
    }

    public abstract String getResourceId(P payVo);

    public abstract Boolean checkBeforeCreatOrder(String userId, P payVo) throws Exception;

    public abstract Boolean checkUserOrderIsMatch(String userId, String orderNo);

    public abstract WxPayCreateOrderParam buildOrderParam(String userId, P payVo,
                                                          HashMap<String, Object> contextMap);

    public abstract P buildPayVoWithReCreatOrder(String userId, String orderNo);

    public abstract Boolean saveOrderInfo(String orderNo, P payVo, WxPayCreateOrderParam orderParam,
                                          HashMap<String, Object> contextMap);

    public abstract Boolean updOrderWithPaySuccess(String orderNo);

    public abstract Boolean closeOrder(String orderNo);

    protected WxPayService getWxPayService() {
        return wxPayService;
    }

    private WxPayUnifiedOrderV3Result.JsapiResult createOrder(WxPayCreateOrderParam orderParam) throws WxPayException {
        validateOrderParam(orderParam);

        String subAppId = StringUtils.defaultIfBlank(orderParam.getSubAppId(), orderParam.getAppId());
        String subOpenId = StringUtils.defaultIfBlank(orderParam.getSubOpenId(), orderParam.getOpenId());
        WxPayPartnerUnifiedOrderV3Request request = new WxPayPartnerUnifiedOrderV3Request();
        request.setSpAppid(StringUtils.defaultIfBlank(orderParam.getSpAppId(), wxPayService.getConfig().getAppId()));
        request.setSpMchId(StringUtils.defaultIfBlank(orderParam.getSpMchId(), wxPayService.getConfig().getMchId()));
        request.setSubAppid(subAppId);
        request.setSubMchId(orderParam.getSubMchId());
        request.setDescription(orderParam.getOrderDesc());
        request.setOutTradeNo(orderParam.getOrderNo());
        request.setTimeExpire(orderParam.getTimeExpire());
        request.setNotifyUrl(wxPayNotifyUrl);
        if (Boolean.TRUE.equals(orderParam.getProfitSharing())) {
            WxPayPartnerUnifiedOrderV3Request.SettleInfo settleInfo = new WxPayPartnerUnifiedOrderV3Request.SettleInfo();
            settleInfo.setProfitSharing(true);
            request.setSettleInfo(settleInfo);
        }

        WxPayPartnerUnifiedOrderV3Request.Amount amountObj = new WxPayPartnerUnifiedOrderV3Request.Amount();
        amountObj.setTotal(orderParam.getAmount());
        amountObj.setCurrency("CNY");
        request.setAmount(amountObj);

        WxPayPartnerUnifiedOrderV3Request.Payer payer = new WxPayPartnerUnifiedOrderV3Request.Payer();
        payer.setSubOpenid(subOpenId);
        request.setPayer(payer);

        return wxPayService.createPartnerOrderV3(TradeTypeEnum.JSAPI, request);
    }

    private void validateOrderParam(WxPayCreateOrderParam orderParam) {
        if (orderParam == null) {
            throw new IllegalArgumentException("支付参数不能为空");
        }
        if (StringUtils.isBlank(orderParam.getOrderNo())) {
            throw new IllegalArgumentException("商户订单号不能为空");
        }
        if (StringUtils.isBlank(orderParam.getOrderDesc())) {
            throw new IllegalArgumentException("订单描述不能为空");
        }
        if (orderParam.getAmount() == null || orderParam.getAmount() <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0分");
        }
        if (StringUtils.isBlank(orderParam.getSubMchId())) {
            throw new IllegalArgumentException("sub_mchid is required for WeChat Pay service provider mode");
        }
        if (StringUtils.isBlank(StringUtils.defaultIfBlank(orderParam.getSubOpenId(), orderParam.getOpenId()))) {
            throw new IllegalArgumentException("sub_openid is required for WeChat Pay service provider mode");
        }
        if (StringUtils.isBlank(StringUtils.defaultIfBlank(orderParam.getSubAppId(), orderParam.getAppId()))) {
            throw new IllegalArgumentException("小程序AppID不能为空");
        }
        if (StringUtils.isBlank(orderParam.getOpenId())) {
            throw new IllegalArgumentException("openid不能为空");
        }
        if (wxPayService == null || wxPayService.getConfig() == null
                || StringUtils.isBlank(wxPayService.getConfig().getAppId())
                || StringUtils.isBlank(wxPayService.getConfig().getMchId())) {
            throw new IllegalStateException("微信支付配置不完整");
        }
        validateNotifyUrl(wxPayNotifyUrl, "支付回调地址");
    }

    private void validateNotifyUrl(String notifyUrl, String label) {
        if (StringUtils.isBlank(notifyUrl)) {
            throw new IllegalStateException(label + "必须配置为公网 HTTPS 地址");
        }
        try {
            URI uri = URI.create(notifyUrl.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || isUnsafeNotifyHost(uri.getHost())) {
                throw new IllegalStateException(label + "必须配置为公网 HTTPS 地址");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(label + "必须配置为公网 HTTPS 地址", e);
        }
    }

    private boolean isUnsafeNotifyHost(String host) {
        if (StringUtils.isBlank(host)) {
            return true;
        }
        String lowerHost = host.toLowerCase();
        return "localhost".equals(lowerHost)
                || lowerHost.endsWith(".localhost")
                || "0.0.0.0".equals(lowerHost)
                || lowerHost.startsWith("127.")
                || lowerHost.startsWith("10.")
                || lowerHost.startsWith("192.168.")
                || lowerHost.matches("^172\\.(1[6-9]|2\\d|3[0-1])\\..*")
                || lowerHost.contains("example")
                || lowerHost.contains("invalid")
                || lowerHost.contains("placeholder")
                || lowerHost.contains("xxx");
    }
}
