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
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
public abstract class AbsWxPayBaseService<P> {

    @Resource
    private WxPayService wxPayService;

    @Value("${wx.pay.notify-url:https://xxx.com/api/wxmini/pay/notify}")
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
        throw new UnsupportedOperationException("微信支付仅支持服务商模式，请在业务服务中实现子商户维度的查询/关单");
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
        validatePartnerOrderParam(orderParam);

        WxPayPartnerUnifiedOrderV3Request partnerRequest = new WxPayPartnerUnifiedOrderV3Request();
        partnerRequest.setSpAppid(wxPayService.getConfig().getAppId());
        partnerRequest.setSpMchId(wxPayService.getConfig().getMchId());
        partnerRequest.setSubAppid(orderParam.getSubAppId());
        partnerRequest.setSubMchId(orderParam.getSubMchId());
        partnerRequest.setDescription(orderParam.getOrderDesc());
        partnerRequest.setOutTradeNo(orderParam.getOrderNo());
        partnerRequest.setTimeExpire(orderParam.getTimeExpire());
        partnerRequest.setNotifyUrl(wxPayNotifyUrl);

        WxPayPartnerUnifiedOrderV3Request.Amount amountObj = new WxPayPartnerUnifiedOrderV3Request.Amount();
        amountObj.setTotal(orderParam.getAmount());
        partnerRequest.setAmount(amountObj);

        WxPayPartnerUnifiedOrderV3Request.Payer payer = new WxPayPartnerUnifiedOrderV3Request.Payer();
        payer.setSubOpenid(orderParam.getOpenId());
        partnerRequest.setPayer(payer);

        return wxPayService.createPartnerOrderV3(TradeTypeEnum.JSAPI, partnerRequest);
    }

    private void validatePartnerOrderParam(WxPayCreateOrderParam orderParam) {
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
        if (StringUtils.isBlank(orderParam.getSubAppId())) {
            throw new IllegalArgumentException("服务商模式下 sub_appid 不能为空");
        }
        if (StringUtils.isBlank(orderParam.getSubMchId())) {
            throw new IllegalArgumentException("服务商模式下 sub_mchid 不能为空");
        }
        if (StringUtils.isBlank(orderParam.getOpenId())) {
            throw new IllegalArgumentException("服务商模式下 sub_openid 不能为空");
        }
        if (wxPayService == null || wxPayService.getConfig() == null
                || StringUtils.isBlank(wxPayService.getConfig().getAppId())
                || StringUtils.isBlank(wxPayService.getConfig().getMchId())) {
            throw new IllegalStateException("微信服务商支付配置不完整");
        }
    }
}
