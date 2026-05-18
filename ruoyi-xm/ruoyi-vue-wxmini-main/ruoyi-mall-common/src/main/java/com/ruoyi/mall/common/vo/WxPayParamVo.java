package com.ruoyi.mall.common.vo;

import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;

/**
 * 小程序端拉起支付用的请求参数
 *
 * @author weijiayu
 * @date 2025/6/11 16:21
 */
public class WxPayParamVo {

    private String orderNo;
    private WxPayUnifiedOrderV3Result.JsapiResult payParam;
    // 历史订单。如果有待支付订单（支付之前可以先核查历史订单），该字段有值
    private String hisOrderNo;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public WxPayUnifiedOrderV3Result.JsapiResult getPayParam() {
        return payParam;
    }

    public void setPayParam(WxPayUnifiedOrderV3Result.JsapiResult payParam) {
        this.payParam = payParam;
    }

    public String getHisOrderNo() {
        return hisOrderNo;
    }

    public void setHisOrderNo(String hisOrderNo) {
        this.hisOrderNo = hisOrderNo;
    }
}
