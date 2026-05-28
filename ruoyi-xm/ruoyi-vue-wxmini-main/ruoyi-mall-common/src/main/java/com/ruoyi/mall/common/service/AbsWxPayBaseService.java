package com.ruoyi.mall.common.service;

import com.github.binarywang.wxpay.bean.request.WxPayOrderQueryV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.mall.common.bo.WxPayCreateOrderParam;
import com.ruoyi.mall.common.vo.WxPayParamVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信小程序支付模板抽象基类
 *
 * @param <P> 支付请求参数
 * @author weijiayu
 * @date 2025/6/12 23:12
 */
@Service
public abstract class AbsWxPayBaseService<P> {

    @Resource
    private WxPayService wxPayService;

    @Value("${wx.pay.notify-url:https://xxx.com/api/wxmini/pay/notify}")
    private String wxPayNotifyUrl;
    // 无锁化的Map+原子操作，记录资源的"占用状态"。synchronized会让同一资源的请求串行化，虽然能保证唯一性，但高并发下会阻塞线程，影响吞吐量。
    private ConcurrentHashMap<String, Object> resourceFlagMap = new ConcurrentHashMap<>();

    public WxPayParamVo createOrder(String userId, P payVo) throws Exception {
        // 获取资源id，尝试占用资源。对资源加锁，避免生成重复订单
        String resourceId = this.getResourceId(payVo);
        if (!(resourceFlagMap.putIfAbsent(resourceId, Boolean.TRUE) == null)) {
            throw new RuntimeException("请稍后再试");
        }
        try {
            // 1、创建订单前的业务核验
            if (!this.checkBeforeCreatOrder(userId, payVo)) {
                return null;
            }
            HashMap<String, Object> contextMap = new HashMap<>();
            // 2、构建订单参数
            WxPayCreateOrderParam orderParam = this.buildOrderParam(userId, payVo, contextMap);
            if (orderParam == null) {
                return null;
            }
            // 3、获取支付参数
            WxPayUnifiedOrderV3Result.JsapiResult jsapiResult = this.createOrder(orderParam);
            if (jsapiResult == null) {
                return null;
            }
            // 4、保存订单信息
            String orderNo = orderParam.getOrderNo();
            if (this.saveOrderInfo(orderNo, payVo, orderParam, contextMap)) {
                WxPayParamVo payParamVo = new WxPayParamVo();
                payParamVo.setOrderNo(orderNo);
                payParamVo.setPayParam(jsapiResult);
                return payParamVo;
            } else {
                return null;
            }
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
        WxPayOrderQueryV3Request request = new WxPayOrderQueryV3Request();
        request.setOutTradeNo(orderNo);
        WxPayOrderQueryV3Result result = wxPayService.queryOrderV3(request);
        Boolean payResult = "SUCCESS".equals(result.getTradeState());
        if (!payResult) {
            wxPayService.closeOrderV3(orderNo);
        }
        this.handlePayResult(payResult, orderNo);
        return payResult;
    }

    public Boolean handlePayResult(Boolean payResult, String orderNo) {
        if (payResult) {
            return this.updOrderWithPaySuccess(orderNo);
        } else {
            return this.closeOrder(orderNo);
        }
    }

    public abstract String getResourceId(P payVo);

    public abstract Boolean checkBeforeCreatOrder(String userId, P payVo) throws Exception;

    public abstract Boolean checkUserOrderIsMatch(String userId, String orderNo);

    public abstract WxPayCreateOrderParam buildOrderParam(String userId, P payVo,
                                                          HashMap<String, Object> contextMap);

    public abstract P buildPayVoWithReCreatOrder(String userId, String orderNo);

    public abstract Boolean saveOrderInfo(String orderNo, P payVo, WxPayCreateOrderParam orderParam, HashMap<String,
            Object> contextMap);

    public abstract Boolean updOrderWithPaySuccess(String orderNo);

    public abstract Boolean closeOrder(String orderNo);

    private WxPayUnifiedOrderV3Result.JsapiResult createOrder(WxPayCreateOrderParam orderParam) throws WxPayException {
        WxPayUnifiedOrderV3Request v3Request = new WxPayUnifiedOrderV3Request();
        v3Request.setAppid(wxPayService.getConfig().getAppId());
        v3Request.setMchid(wxPayService.getConfig().getMchId());
        v3Request.setDescription(orderParam.getOrderDesc());
        v3Request.setOutTradeNo(orderParam.getOrderNo());
        v3Request.setTimeExpire(orderParam.getTimeExpire());

        v3Request.setNotifyUrl(wxPayNotifyUrl);

        WxPayUnifiedOrderV3Request.Amount amountObj = new WxPayUnifiedOrderV3Request.Amount();
        amountObj.setTotal(orderParam.getAmount()); // 单位分
        v3Request.setAmount(amountObj);

        WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
        payer.setOpenid(orderParam.getOpenId());
        v3Request.setPayer(payer);

        return wxPayService.createOrderV3(TradeTypeEnum.JSAPI, v3Request);
    }
}
