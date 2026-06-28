package com.ruoyi.mall.common.service;

import com.github.binarywang.wxpay.exception.WxPayException;
import com.ruoyi.mall.common.vo.WxPayParamVo;

/**
 * 微信小程序支付接口
 *
 * @param <P> 支付请求参数
 * @author weijiayu
 * @date 2025/6/14 20:47
 */
public interface IWxPayBaseService<P> {

    /**
     * 创建支付订单
     */
    public WxPayParamVo createOrder(String userId, P payVo) throws Exception;

    /**
     * 重新支付历史订单
     */
    public WxPayParamVo reCreatOrder(String userId, String orderNo) throws Exception;

    /**
     * 取消支付订单
     */
    public Boolean cancelOrder(String userId, String orderNo) throws WxPayException;

    /**
     * 查询支付结果，并立即更新订单状态（有副作用）
     */
    public Boolean queryPayResultAndUpdOrderStatus(String orderNo) throws WxPayException;

    /**
     * 关闭待支付订单
     */
    public Boolean closeOrder(String orderNo);

    /**
     * 处理支付结果
     */
    public Boolean handlePayResult(Boolean payResult, String orderNo);
}
