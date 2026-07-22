package com.ruoyi.wxmini.listener;

import com.github.binarywang.wxpay.bean.request.WxPayPartnerRefundV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.mall.common.event.RefundApprovedEvent;
import com.ruoyi.mall.common.event.RefundSucceededEvent;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import com.ruoyi.mall.pay.service.IPaymentRecordService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Component
public class WxRefundEventListener {

    private static final Logger log = LoggerFactory.getLogger(WxRefundEventListener.class);

    @Value("${wx.pay.refund-notify-url}")
    private String refundNotifyUrl;
    @Value("${wx.pay.refund-retry-max-attempts:6}")
    private int refundRetryMaxAttempts;
    @Value("${wx.pay.refund-retry-base-delay-ms:300000}")
    private long refundRetryBaseDelayMs;
    @Value("${wx.pay.refund-retry-max-delay-ms:21600000}")
    private long refundRetryMaxDelayMs;
    @Value("${wx.pay.refund-retry-attempt-lease-ms:900000}")
    private long refundRetryAttemptLeaseMs;

    private static final int RETRY_BATCH_SIZE = 20;
    private static final int MAX_RETRY_REASON_LENGTH = 500;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private WxPayService wxPayService;
    @Resource
    private RefundRecordMapper refundRecordMapper;
    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private IPaymentRecordService paymentRecordService;
    @Resource
    private ApplicationContext applicationContext;

    @EventListener
    @Async
    public void onRefundApproved(RefundApprovedEvent event) {
        requestWxRefund(event.getOrderNo(), event.getRefundRecordId(), false);
    }

    @Scheduled(initialDelayString = "${wx.pay.refund-retry-initial-delay-ms:120000}",
            fixedDelayString = "${wx.pay.refund-retry-fixed-delay-ms:300000}")
    public void retryApprovedRefunds() {
        if (wxPayService == null) {
            return;
        }
        List<RefundRecord> pendingRefunds = refundRecordMapper.selectRetryableApprovedRefunds(RETRY_BATCH_SIZE);
        if (pendingRefunds == null || pendingRefunds.isEmpty()) {
            return;
        }
        int handled = 0;
        for (RefundRecord refundRecord : pendingRefunds) {
            if (refundRecord == null || refundRecord.getId() == null
                    || StringUtils.isBlank(refundRecord.getOrderNo())) {
                continue;
            }
            requestWxRefund(refundRecord.getOrderNo(), refundRecord.getId(), true);
            handled++;
            if (handled >= RETRY_BATCH_SIZE) {
                break;
            }
        }
    }

    private void requestWxRefund(String orderNo, Long refundRecordId, boolean retry) {
        if (wxPayService == null) {
            log.info("WxPayService未配置，跳过微信退款，orderNo={}", orderNo);
            return;
        }
        log.info("{}微信退款API: orderNo={}, refundId={}",
                retry ? "补偿重试" : "收到退款审核通过事件，准备调用", orderNo, refundRecordId);

        RefundRecord refundRecord = null;
        try {
            refundRecord = refundRecordMapper.selectRefundRecordById(refundRecordId);
            if (refundRecord == null) {
                log.error("退款记录不存在: refundId={}", refundRecordId);
                return;
            }
            if (refundRecord.getStatus() == null || refundRecord.getStatus() != RefundRecord.STATUS_APPROVED) {
                log.warn("退款记录状态不是待微信退款，跳过: refundId={}, status={}",
                        refundRecordId, refundRecord.getStatus());
                return;
            }
            Date attemptLeaseUntil = new Date(System.currentTimeMillis() + normalizedAttemptLeaseMillis());
            if (refundRecordMapper.claimApprovedRefundAttempt(refundRecord.getId(), attemptLeaseUntil) == 0) {
                log.info("退款已由其他执行方处理，跳过本次尝试: refundId={}", refundRecordId);
                return;
            }

            MallOrder order = mallOrderMapper.selectMallOrderByOrderNo(orderNo);
            if (order == null) {
                log.error("原订单不存在: orderNo={}", orderNo);
                markRefundAbnormal(refundRecord, "Original order not found");
                return;
            }

            Merchant merchant = merchantService.selectMerchantById(order.getMerchantId());
            if (merchant == null || StringUtils.isBlank(merchant.getEffectiveMerchantWxMchId())) {
                log.error("sub_mchid is missing for refund: orderNo={}, merchantId={}", orderNo, order.getMerchantId());
                markRefundAbnormal(refundRecord, "Missing merchant sub_mchid");
                return;
            }

            WxPayPartnerRefundV3Request request = new WxPayPartnerRefundV3Request();
            request.setOutTradeNo(orderNo);
            request.setSubMchid(merchant.getEffectiveMerchantWxMchId());
            request.setOutRefundNo(refundRecord.getRefundNo() != null ? refundRecord.getRefundNo() : "RF_" + orderNo);
            request.setReason(refundRecord.getRefundReason() != null ? refundRecord.getRefundReason() : "用户申请退款");

            int totalAmountFen = toFen(order.getPayAmount());
            int refundAmountFen = toFen(refundRecord.getRefundAmount() != null
                    ? refundRecord.getRefundAmount() : order.getPayAmount());
            if (refundAmountFen <= 0 || refundAmountFen > totalAmountFen) {
                log.error("退款金额非法: orderNo={}, totalFen={}, refundFen={}",
                        orderNo, totalAmountFen, refundAmountFen);
                markRefundAbnormal(refundRecord, "Refund amount is invalid");
                return;
            }

            WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
            amount.setRefund(refundAmountFen);
            amount.setTotal(totalAmountFen);
            amount.setCurrency("CNY");
            request.setAmount(amount);
            validateNotifyUrl(refundNotifyUrl, "退款回调地址");
            request.setNotifyUrl(refundNotifyUrl);

            log.info("调用微信退款API: orderNo={}, outRefundNo={}, subMchid={}, totalFen={}, refundFen={}",
                    orderNo, request.getOutRefundNo(), request.getSubMchid(), totalAmountFen, refundAmountFen);
            WxPayRefundV3Result result = wxPayService.refundV3(request);
            log.info("微信退款API调用成功: orderNo={}, refundNo={}, status={}",
                    orderNo, result.getOutRefundNo(), result.getStatus());

            if (StringUtils.isNotBlank(result.getOutRefundNo())) {
                refundRecordMapper.updateRefundNoForApproved(refundRecord.getId(), result.getOutRefundNo());
            }
            if ("SUCCESS".equals(result.getStatus())) {
                completeRefund(order, refundRecord, result.getOutRefundNo());
            } else if ("ABNORMAL".equals(result.getStatus()) || "CLOSED".equals(result.getStatus())) {
                markRefundAbnormal(refundRecord, "WeChat refund status: " + result.getStatus());
            } else {
                scheduleRefundRetry(refundRecord, "WeChat refund status: "
                        + StringUtils.defaultIfBlank(result.getStatus(), "UNKNOWN"));
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("微信退款请求参数或配置无效: orderNo={}, error={}", orderNo, e.getMessage(), e);
            if (refundRecord != null && refundRecord.getStatus() != null
                    && refundRecord.getStatus() == RefundRecord.STATUS_APPROVED) {
                markRefundAbnormal(refundRecord, "Invalid refund request or configuration: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("微信退款API暂时调用失败，将按退避重试: orderNo={}, error={}",
                    orderNo, e.getMessage(), e);
            if (refundRecord != null && refundRecord.getStatus() != null
                    && refundRecord.getStatus() == RefundRecord.STATUS_APPROVED) {
                scheduleRefundRetry(refundRecord, "WeChat refund request failed: " + e.getMessage());
            }
        }
    }

    private void completeRefund(MallOrder order, RefundRecord refundRecord, String outRefundNo) {
        Date refundTime = new Date();
        int affectedRows = refundRecordMapper.markRefundSucceeded(refundRecord.getId(), refundTime);
        if (affectedRows <= 0) {
            log.info("退款记录已完成或状态不可迁移，跳过本地重复收口: orderNo={}, refundId={}, refundNo={}",
                    order.getOrderNo(), refundRecord.getId(), outRefundNo);
            return;
        }

        mallOrderMapper.markOrderRefunded(order.getOrderNo(), refundTime);
        paymentRecordService.markRefunded(order.getOrderNo(), buildRefundNotifyResult("SUCCESS"));
        applicationContext.publishEvent(new RefundSucceededEvent(
                this, order.getOrderNo(), refundRecord.getId(), outRefundNo));
        log.info("微信退款查询已确认成功，本地退款状态已完成: orderNo={}, refundId={}, refundNo={}",
                order.getOrderNo(), refundRecord.getId(), outRefundNo);
    }

    private String buildRefundNotifyResult(String refundStatus) {
        return "REFUND:" + StringUtils.defaultIfBlank(refundStatus, "UNKNOWN");
    }

    private int toFen(BigDecimal amount) {
        if (amount == null) {
            return 0;
        }
        return amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).intValueExact();
    }

    private void markRefundAbnormal(RefundRecord refundRecord, String reason) {
        refundRecordMapper.markRefundAbnormalWithReason(refundRecord.getId(), abbreviateReason(reason));
    }

    private void scheduleRefundRetry(RefundRecord refundRecord, String reason) {
        int retryCount = refundRecord.getRetryCount() == null ? 0 : refundRecord.getRetryCount();
        int nextAttempt = retryCount + 1;
        Date nextRetryTime = nextAttempt >= normalizedMaxRetryAttempts()
                ? null : new Date(System.currentTimeMillis() + calculateRetryDelayMillis(nextAttempt));
        int updated = refundRecordMapper.scheduleRefundRetry(refundRecord.getId(), nextRetryTime,
                abbreviateReason(reason), normalizedMaxRetryAttempts());
        if (updated > 0) {
            if (nextRetryTime == null) {
                log.error("Refund retry limit reached and marked abnormal: refundId={}, attempts={}",
                        refundRecord.getId(), nextAttempt);
            } else {
                log.warn("Refund retry scheduled: refundId={}, attempt={}, nextRetryTime={}",
                        refundRecord.getId(), nextAttempt, nextRetryTime);
            }
        }
    }

    private int normalizedMaxRetryAttempts() {
        return Math.max(1, refundRetryMaxAttempts);
    }

    private long calculateRetryDelayMillis(int attempt) {
        long baseDelay = Math.max(1000L, refundRetryBaseDelayMs);
        long maxDelay = Math.max(baseDelay, refundRetryMaxDelayMs);
        long delay = baseDelay;
        for (int i = 1; i < attempt && delay < maxDelay; i++) {
            delay = Math.min(maxDelay, delay > Long.MAX_VALUE / 2 ? maxDelay : delay * 2);
        }
        return delay;
    }

    private long normalizedAttemptLeaseMillis() {
        return Math.max(1000L, refundRetryAttemptLeaseMs);
    }

    private String abbreviateReason(String reason) {
        String normalized = StringUtils.defaultIfBlank(reason, "Unknown refund retry failure").trim();
        return normalized.length() <= MAX_RETRY_REASON_LENGTH
                ? normalized : normalized.substring(0, MAX_RETRY_REASON_LENGTH);
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
