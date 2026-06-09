package com.ruoyi.wxmini.listener;

import com.github.binarywang.wxpay.bean.request.WxPayPartnerRefundV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.mall.common.event.RefundApprovedEvent;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class WxRefundEventListener {

    private static final Logger log = LoggerFactory.getLogger(WxRefundEventListener.class);

    @Value("${wx.pay.refund-notify-url}")
    private String refundNotifyUrl;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private WxPayService wxPayService;
    @Resource
    private RefundRecordMapper refundRecordMapper;
    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private IMerchantService merchantService;

    @EventListener
    @Async
    public void onRefundApproved(RefundApprovedEvent event) {
        if (wxPayService == null) {
            log.info("WxPayService未配置，跳过微信退款，orderNo={}", event.getOrderNo());
            return;
        }

        String orderNo = event.getOrderNo();
        Long refundRecordId = event.getRefundRecordId();
        log.info("收到退款审核通过事件，准备调用微信退款API: orderNo={}, refundId={}", orderNo, refundRecordId);

        try {
            RefundRecord refundRecord = refundRecordMapper.selectRefundRecordById(refundRecordId);
            if (refundRecord == null) {
                log.error("退款记录不存在: refundId={}", refundRecordId);
                return;
            }
            if (refundRecord.getStatus() == null || refundRecord.getStatus() != RefundRecord.STATUS_APPROVED) {
                log.warn("退款记录状态不是待微信退款，跳过: refundId={}, status={}",
                        refundRecordId, refundRecord.getStatus());
                return;
            }

            MallOrder order = mallOrderMapper.selectMallOrderByOrderNo(orderNo);
            if (order == null) {
                log.error("原订单不存在: orderNo={}", orderNo);
                return;
            }

            Merchant merchant = merchantService.selectMerchantById(order.getMerchantId());
            if (merchant == null || StringUtils.isBlank(merchant.getEffectiveMerchantWxMchId())) {
                log.error("商户支付配置不完整: merchantId={}", order.getMerchantId());
                return;
            }

            WxPayPartnerRefundV3Request request = new WxPayPartnerRefundV3Request();
            request.setOutTradeNo(orderNo);
            request.setOutRefundNo(refundRecord.getRefundNo() != null ? refundRecord.getRefundNo() : "RF_" + orderNo);
            request.setReason(refundRecord.getRefundReason() != null ? refundRecord.getRefundReason() : "用户申请退款");
            request.setSubMchid(merchant.getEffectiveMerchantWxMchId());

            int totalAmountFen = toFen(order.getPayAmount());
            int refundAmountFen = toFen(refundRecord.getRefundAmount() != null
                    ? refundRecord.getRefundAmount() : order.getPayAmount());
            if (refundAmountFen <= 0 || refundAmountFen > totalAmountFen) {
                log.error("退款金额非法: orderNo={}, totalFen={}, refundFen={}",
                        orderNo, totalAmountFen, refundAmountFen);
                markRefundAbnormal(refundRecord);
                return;
            }

            WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
            amount.setRefund(refundAmountFen);
            amount.setTotal(totalAmountFen);
            amount.setCurrency("CNY");
            request.setAmount(amount);
            validateNotifyUrl(refundNotifyUrl, "退款回调地址");
            request.setNotifyUrl(refundNotifyUrl);

            WxPayRefundV3Result result = wxPayService.refundV3(request);
            log.info("微信退款API调用成功: orderNo={}, refundNo={}, status={}",
                    orderNo, result.getOutRefundNo(), result.getStatus());

            if (result.getRefundId() != null) {
                refundRecord.setRefundNo(result.getOutRefundNo());
                refundRecordMapper.updateRefundRecord(refundRecord);
            }
            if ("ABNORMAL".equals(result.getStatus()) || "CLOSED".equals(result.getStatus())) {
                markRefundAbnormal(refundRecord);
            }
        } catch (Exception e) {
            log.error("微信退款API调用失败: orderNo={}, error={}", orderNo, e.getMessage(), e);
        }
    }

    private int toFen(BigDecimal amount) {
        if (amount == null) {
            return 0;
        }
        return amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).intValueExact();
    }

    private void markRefundAbnormal(RefundRecord refundRecord) {
        refundRecord.setStatus(RefundRecord.STATUS_ABNORMAL);
        refundRecordMapper.updateRefundRecord(refundRecord);
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
