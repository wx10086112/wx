package com.ruoyi.wxmini.service.impl;

import com.ruoyi.mall.common.bo.WxPayCreateOrderParam;
import com.ruoyi.mall.common.service.AbsWxPayBaseService;
import com.ruoyi.mall.common.service.IWxPayOrderService;
import com.ruoyi.mall.common.vo.WxPayOrderVo;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.service.IMallOrderService;
import com.ruoyi.mall.pay.service.IPaymentRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

/**
 * 订单支付服务实现（真实微信支付）
 */
@Service
public class WxPayOrderServiceImpl extends AbsWxPayBaseService<WxPayOrderVo> implements IWxPayOrderService {

    private static final Logger log = LoggerFactory.getLogger(WxPayOrderServiceImpl.class);

    private static final int ORDER_STATUS_PENDING = 0;
    private static final int ORDER_STATUS_PAID = 1;
    private static final int ORDER_STATUS_CLOSED = 2;

    @Resource
    private IMallOrderService mallOrderService;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private IPaymentRecordService paymentRecordService;

    @Override
    public String getResourceId(WxPayOrderVo payVo) {
        return "pay_order_" + payVo.getOrderNo();
    }

    @Override
    public Boolean checkBeforeCreatOrder(String userId, WxPayOrderVo payVo) throws Exception {
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(payVo.getOrderNo());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().toString().equals(userId)) {
            throw new RuntimeException("无权支付该订单");
        }
        if (order.getStatus() == null || order.getStatus() != ORDER_STATUS_PENDING) {
            throw new RuntimeException("当前订单状态不可支付");
        }
        return true;
    }

    @Override
    public Boolean checkUserOrderIsMatch(String userId, String orderNo) {
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        return order != null && order.getUserId().toString().equals(userId);
    }

    @Override
    public WxPayCreateOrderParam buildOrderParam(String userId, WxPayOrderVo payVo, HashMap<String, Object> contextMap) {
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(payVo.getOrderNo());
        if (order == null) {
            return null;
        }
        Merchant merchant = merchantService.selectMerchantById(order.getMerchantId());

        WxPayCreateOrderParam param = new WxPayCreateOrderParam();
        param.setOrderNo(order.getOrderNo());
        param.setOrderDesc("团购订单-" + order.getOrderNo());
        // 金额转为分（微信支付单位为分）
        int amountFen = order.getPayAmount().multiply(BigDecimal.valueOf(100)).intValue();
        param.setAmount(amountFen);
        // 从上下文或用户信息获取 openid（需在调用前设置）
        String openId = (String) contextMap.get("openId");
        param.setOpenId(openId);
        // 订单超时30分钟
        param.setTimeExpire(formatExpireTime(30 * 60));

        contextMap.put("merchantId", order.getMerchantId());
        return param;
    }

    @Override
    public WxPayOrderVo buildPayVoWithReCreatOrder(String userId, String orderNo) {
        WxPayOrderVo vo = new WxPayOrderVo();
        vo.setOrderNo(orderNo);
        return vo;
    }

    @Override
    public Boolean saveOrderInfo(String orderNo, WxPayOrderVo payVo, WxPayCreateOrderParam orderParam, HashMap<String, Object> contextMap) {
        // 创建支付记录（全链路留痕）
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        if (order != null) {
            paymentRecordService.createPayment(orderNo, order.getMerchantId(), order.getUserId(), order.getPayAmount(), orderNo);
        }
        log.info("订单{}支付参数已生成，等待用户支付", orderNo);
        return true;
    }

    @Override
    public Boolean updOrderWithPaySuccess(String orderNo) {
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        if (order == null) {
            log.error("支付成功但订单不存在: {}", orderNo);
            return false;
        }
        if (order.getStatus() != null && order.getStatus() >= ORDER_STATUS_PAID) {
            log.info("订单{}已支付，跳过重复更新（幂等）", orderNo);
            return true;
        }
        order.setStatus(ORDER_STATUS_PAID);
        order.setPayTime(new Date());
        mallOrderService.updateMallOrder(order);
        log.info("订单{}支付成功，状态更新为已支付", orderNo);
        return true;
    }

    @Override
    public Boolean closeOrder(String orderNo) {
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        if (order == null) {
            return false;
        }
        if (order.getStatus() != null && order.getStatus() == ORDER_STATUS_PENDING) {
            order.setStatus(ORDER_STATUS_CLOSED);
            order.setCancelTime(new Date());
            mallOrderService.updateMallOrder(order);
            log.info("订单{}已关闭", orderNo);
        }
        return true;
    }

    private String formatExpireTime(int seconds) {
        long expireMillis = System.currentTimeMillis() + seconds * 1000L;
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date(expireMillis));
    }
}
