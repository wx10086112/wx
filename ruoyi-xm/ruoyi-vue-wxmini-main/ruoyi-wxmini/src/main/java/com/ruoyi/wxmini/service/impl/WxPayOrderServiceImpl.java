package com.ruoyi.wxmini.service.impl;

import com.github.binarywang.wxpay.bean.request.WxPayPartnerOrderCloseV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayPartnerOrderQueryV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayPartnerOrderQueryV3Result;
import com.ruoyi.mall.common.bo.WxPayCreateOrderParam;
import com.ruoyi.mall.common.service.AbsWxPayBaseService;
import com.ruoyi.mall.common.service.IWxPayOrderService;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.common.vo.WxPayOrderVo;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.constant.MallOrderStatus;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.service.IMallOrderService;
import com.ruoyi.mall.pay.service.IPaymentRecordService;
import com.ruoyi.mall.product.domain.Distributor;
import com.ruoyi.mall.product.service.IDistributorService;
import com.ruoyi.mall.user.domain.UserInfo;
import com.ruoyi.mall.user.service.IUserInfoService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;

@Service
public class WxPayOrderServiceImpl extends AbsWxPayBaseService<WxPayOrderVo> implements IWxPayOrderService {

    private static final Logger log = LoggerFactory.getLogger(WxPayOrderServiceImpl.class);

    @Resource
    private IMallOrderService mallOrderService;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private IPaymentRecordService paymentRecordService;
    @Resource
    private IUserInfoService userInfoService;
    @Resource
    private IDistributorService distributorService;

    @Override
    public String getResourceId(WxPayOrderVo payVo) {
        return "pay_order_" + payVo.getOrderNo();
    }

    @Override
    public Boolean checkBeforeCreatOrder(String userId, WxPayOrderVo payVo) {
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(payVo.getOrderNo());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!isUserOrder(userId, order)) {
            throw new RuntimeException("无权支付该订单");
        }
        checkOrderTenant(order);
        if (order.getStatus() == null || order.getStatus() != MallOrderStatus.PENDING) {
            throw new RuntimeException("当前订单状态不可支付");
        }
        return true;
    }

    @Override
    public Boolean checkUserOrderIsMatch(String userId, String orderNo) {
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        return order != null && isUserOrder(userId, order);
    }

    @Override
    public WxPayCreateOrderParam buildOrderParam(String userId, WxPayOrderVo payVo, HashMap<String, Object> contextMap) {
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(payVo.getOrderNo());
        if (order == null) {
            return null;
        }
        checkOrderTenant(order);
        if (StringUtils.isBlank(payVo.getOpenId())) {
            throw new IllegalArgumentException("openId不能为空");
        }

        Merchant merchant = requireMerchant(order.getMerchantId());
        String blockReason = merchant.getOperateBlockReason();
        if (StringUtils.isNotBlank(blockReason)) {
            throw new RuntimeException("商户支付配置不完整: " + blockReason);
        }
        checkDistributorSettlementReceiver(merchant);
        if (StringUtils.isBlank(merchant.getCMiniAppId())) {
            throw new RuntimeException("商户小程序AppID未配置");
        }
        if (StringUtils.isBlank(merchant.getEffectiveMerchantWxMchId())) {
            throw new RuntimeException("sub_mchid is required for WeChat Pay service provider mode");
        }
        WxPayCreateOrderParam param = new WxPayCreateOrderParam();
        param.setOrderNo(order.getOrderNo());
        param.setOrderDesc("团购订单-" + order.getOrderNo());
        param.setAmount(toFenExact(order.getPayAmount()));
        param.setAppId(merchant.getCMiniAppId());
        param.setOpenId(payVo.getOpenId());
        param.setSpAppId(getWxPayService().getConfig().getAppId());
        param.setSpMchId(getWxPayService().getConfig().getMchId());
        param.setSubAppId(merchant.getCMiniAppId());
        param.setSubMchId(merchant.getEffectiveMerchantWxMchId());
        param.setSubOpenId(payVo.getOpenId());
        param.setTimeExpire(formatExpireTime(30 * 60));

        contextMap.put("merchantId", order.getMerchantId());
        contextMap.put("spMchId", param.getSpMchId());
        contextMap.put("subMchId", param.getSubMchId());
        contextMap.put("subAppId", param.getSubAppId());
        contextMap.put("payerOpenid", param.getSubOpenId());
        return param;
    }

    @Override
    public WxPayOrderVo buildPayVoWithReCreatOrder(String userId, String orderNo) {
        WxPayOrderVo vo = new WxPayOrderVo();
        vo.setOrderNo(orderNo);
        return vo;
    }

    @Override
    public Boolean saveOrderInfo(String orderNo, WxPayOrderVo payVo, WxPayCreateOrderParam orderParam,
                                 HashMap<String, Object> contextMap) {
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        if (order != null) {
            paymentRecordService.createPayment(orderNo, order.getMerchantId(), order.getUserId(), order.getPayAmount(), orderNo,
                    orderParam.getSpMchId(), orderParam.getSubMchId(), orderParam.getSubAppId(), orderParam.getSubOpenId());
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
        if (MallOrderStatus.isPaidState(order.getStatus())) {
            log.info("订单{}已处于支付完成链路，跳过重复更新", orderNo);
            return true;
        }
        boolean markedPaid = mallOrderService.markOrderPaid(orderNo, new Date());
        if (markedPaid) {
            log.info("订单{}支付成功，状态更新为已支付", orderNo);
            return true;
        }

        MallOrder latestOrder = mallOrderService.selectMallOrderByOrderNo(orderNo);
        if (latestOrder != null && MallOrderStatus.isPaidState(latestOrder.getStatus())) {
            log.info("订单{}已由其他支付链路更新为已支付状态", orderNo);
            return true;
        } else {
            log.info("订单{}支付成功处理时状态已变更，跳过重复更新", orderNo);
        }
        return false;
    }

    @Override
    public Boolean closeOrder(String orderNo) {
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        if (order == null) {
            return false;
        }
        if (order.getStatus() == null || order.getStatus() != MallOrderStatus.PENDING) {
            return true;
        }
        boolean closed = mallOrderService.cancelPendingOrder(orderNo);
        if (closed) {
            log.info("订单{}已关闭", orderNo);
        }
        return closed;
    }

    @Override
    public Boolean queryPayResultAndUpdOrderStatus(String orderNo) throws com.github.binarywang.wxpay.exception.WxPayException {
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        if (order == null) {
            return false;
        }
        checkOrderTenant(order);
        Merchant merchant = requireMerchant(order.getMerchantId());
        WxPayPartnerOrderQueryV3Request request = new WxPayPartnerOrderQueryV3Request()
                .setOutTradeNo(orderNo)
                .setSpMchId(getWxPayService().getConfig().getMchId())
                .setSubMchId(merchant.getEffectiveMerchantWxMchId());
        WxPayPartnerOrderQueryV3Result result = getWxPayService().queryPartnerOrderV3(request);
        boolean payResult = "SUCCESS".equals(result.getTradeState()) && isSamePartnerPayment(merchant, result);
        if ("SUCCESS".equals(result.getTradeState()) && !payResult) {
            log.warn("微信支付查单归属不匹配，拒绝更新订单状态: orderNo={}, spMchId={}, subMchId={}, subAppId={}",
                    orderNo, result.getSpMchId(), result.getSubMchId(), result.getSubAppid());
            throw new IllegalStateException("WeChat Pay partner order ownership mismatch");
        }
        if (!payResult) {
            WxPayPartnerOrderCloseV3Request closeRequest = new WxPayPartnerOrderCloseV3Request()
                    .setOutTradeNo(orderNo)
                    .setSpMchId(getWxPayService().getConfig().getMchId())
                    .setSubMchId(merchant.getEffectiveMerchantWxMchId());
            getWxPayService().closePartnerOrderV3(closeRequest);
        }
        this.handlePayResult(payResult, orderNo);
        return payResult;
    }

    private boolean isSamePartnerPayment(Merchant merchant, WxPayPartnerOrderQueryV3Result result) {
        return merchant != null
                && getWxPayService() != null
                && getWxPayService().getConfig() != null
                && StringUtils.equals(result.getSpMchId(), getWxPayService().getConfig().getMchId())
                && StringUtils.equals(result.getSubMchId(), merchant.getEffectiveMerchantWxMchId())
                && StringUtils.equals(result.getSubAppid(), merchant.getCMiniAppId());
    }

    private boolean isUserOrder(String userId, MallOrder order) {
        if (StringUtils.isBlank(userId) || order == null || order.getUserId() == null) {
            return false;
        }
        UserInfo userInfo = userInfoService.selectUserInfoByUserId(userId);
        return userInfo != null && userInfo.getId() != null && order.getUserId().equals(userInfo.getId());
    }

    private Merchant requireMerchant(Long merchantId) {
        Merchant merchant = merchantService.selectMerchantById(merchantId);
        if (merchant == null) {
            throw new RuntimeException("商户不存在");
        }
        return merchant;
    }

    private void checkDistributorSettlementReceiver(Merchant merchant) {
        if (merchant.getDistributorId() == null
                || merchant.getDistributorShareRate() == null
                || merchant.getDistributorShareRate().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        Distributor distributor = distributorService.selectDistributorById(merchant.getDistributorId());
        if (distributor == null || StringUtils.isBlank(distributor.getReceiverOpenid())) {
            throw new RuntimeException("distributor receiver_openid is required for T+1 settlement");
        }
    }

    private void checkOrderTenant(MallOrder order) {
        Long tokenMerchantId = WxMiniUserContext.getCurrentMerchantId();
        if (tokenMerchantId == null || !tokenMerchantId.equals(order.getMerchantId())) {
            throw new RuntimeException("订单商户与当前小程序登录态不匹配");
        }
        Long appIdMerchantId = WxMiniUserContext.getAppIdMerchantId();
        if (appIdMerchantId != null && !appIdMerchantId.equals(order.getMerchantId())) {
            throw new RuntimeException("订单商户与当前小程序AppID不匹配");
        }
    }

    private int toFenExact(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0元");
        }
        return amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).intValueExact();
    }

    private String formatExpireTime(int seconds) {
        long expireMillis = System.currentTimeMillis() + seconds * 1000L;
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date(expireMillis));
    }
}
