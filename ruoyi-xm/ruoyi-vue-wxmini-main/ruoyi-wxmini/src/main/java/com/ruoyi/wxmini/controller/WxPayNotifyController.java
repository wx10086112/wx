package com.ruoyi.wxmini.controller;

import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyV3Result;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.mall.common.event.RefundSucceededEvent;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.constant.MallOrderStatus;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import com.ruoyi.mall.order.service.IMallOrderService;
import com.ruoyi.mall.pay.service.IPaymentRecordService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.Date;

@RestController
@RequestMapping("/wxmini/pay")
public class WxPayNotifyController {

    private static final Logger log = LoggerFactory.getLogger(WxPayNotifyController.class);

    @Autowired(required = false)
    private WxPayService wxPayService;
    @Autowired
    private IMallOrderService mallOrderService;
    @Autowired
    private IPaymentRecordService paymentRecordService;
    @Autowired
    private RefundRecordMapper refundRecordMapper;
    @Autowired
    private IMerchantService merchantService;
    @Autowired
    private ApplicationContext applicationContext;

    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) {
        try {
            if (wxPayService == null) {
                log.warn("WxPayService未配置，忽略支付回调");
                return buildSuccessResponse();
            }

            String body = readRequestBody(request);
            SignatureHeader header = new SignatureHeader();
            header.setTimeStamp(request.getHeader("Wechatpay-Timestamp"));
            header.setNonce(request.getHeader("Wechatpay-Nonce"));
            header.setSignature(request.getHeader("Wechatpay-Signature"));
            header.setSerial(request.getHeader("Wechatpay-Serial"));

            WxPayNotifyV3Result result = wxPayService.parseOrderNotifyV3Result(body, header);
            String outTradeNo = result.getResult().getOutTradeNo();
            String transactionId = result.getResult().getTransactionId();
            String tradeState = result.getResult().getTradeState();
            String mchId = result.getResult().getMchid();
            String appId = result.getResult().getAppid();

            MallOrder order = mallOrderService.selectMallOrderByOrderNo(outTradeNo);
            if (order == null) {
                log.error("支付回调订单不存在: {}", outTradeNo);
                return buildSuccessResponse();
            }
            if (!validatePlatformPayNotify(order, mchId, appId)) {
                log.error("支付回调商户信息不匹配: orderNo={}, mchId={}, appId={}",
                        outTradeNo, mchId, appId);
                return buildErrorResponse("商户信息不匹配");
            }

            if (MallOrderStatus.isPaidState(order.getStatus())) {
                if ("SUCCESS".equals(tradeState)) {
                    paymentRecordService.markPaySuccess(outTradeNo, order.getMerchantId(), order.getUserId(),
                            order.getPayAmount(), transactionId, body);
                }
                log.info("订单{}已处于支付完成链路，跳过重复处理", outTradeNo);
                return buildSuccessResponse();
            }

            if ("SUCCESS".equals(tradeState)) {
                Date payTime = new Date();
                boolean markedPaid = mallOrderService.markOrderPaid(outTradeNo, payTime);
                paymentRecordService.markPaySuccess(outTradeNo, order.getMerchantId(), order.getUserId(),
                        order.getPayAmount(), transactionId, body);
                if (markedPaid) {
                    log.info("订单{}支付成功，transactionId={}", outTradeNo, transactionId);
                } else {
                    log.info("订单{}支付回调重复或状态已变更，跳过订单状态迁移", outTradeNo);
                }
            } else {
                log.warn("订单{}支付状态非SUCCESS: {}", outTradeNo, tradeState);
            }

            return buildSuccessResponse();
        } catch (Exception e) {
            log.error("处理微信支付回调异常", e);
            return buildErrorResponse("处理失败");
        }
    }

    @PostMapping("/refund-notify")
    public String refundNotify(HttpServletRequest request) {
        try {
            if (wxPayService == null) {
                log.warn("WxPayService未配置，忽略退款回调");
                return buildSuccessResponse();
            }

            String body = readRequestBody(request);
            SignatureHeader header = new SignatureHeader();
            header.setTimeStamp(request.getHeader("Wechatpay-Timestamp"));
            header.setNonce(request.getHeader("Wechatpay-Nonce"));
            header.setSignature(request.getHeader("Wechatpay-Signature"));
            header.setSerial(request.getHeader("Wechatpay-Serial"));

            WxPayRefundNotifyV3Result result = wxPayService.parseRefundNotifyV3Result(body, header);
            String outRefundNo = result.getResult().getOutRefundNo();
            String refundStatus = result.getResult().getRefundStatus();
            String mchId = result.getResult().getMchid();
            RefundRecord refundRecord = refundRecordMapper.selectRefundRecordByRefundNo(outRefundNo);

            if (refundRecord == null) {
                log.warn("退款回调未找到退款记录: refundNo={}", outRefundNo);
                return buildSuccessResponse();
            }
            MallOrder order = mallOrderService.selectMallOrderByOrderNo(refundRecord.getOrderNo());
            if (order == null) {
                log.error("退款回调订单不存在: orderNo={}, refundNo={}", refundRecord.getOrderNo(), outRefundNo);
                return buildErrorResponse("订单不存在");
            }
            if (!validatePlatformMchId(mchId)) {
                log.error("退款回调商户信息不匹配: orderNo={}, refundNo={}, mchId={}",
                        order.getOrderNo(), outRefundNo, mchId);
                return buildErrorResponse("商户信息不匹配");
            }

            if ("SUCCESS".equals(refundStatus)) {
                Date refundTime = new Date();
                int affectedRows = refundRecordMapper.markRefundSucceeded(refundRecord.getId(), refundTime);
                if (affectedRows > 0) {
                    refundRecord.setRefundTime(refundTime);
                    boolean orderMarkedRefunded = mallOrderService.markOrderRefunded(order.getOrderNo(), refundRecord.getRefundTime());
                    if (orderMarkedRefunded) {
                        paymentRecordService.markRefunded(order.getOrderNo(), body);
                        applicationContext.publishEvent(new RefundSucceededEvent(
                                this, order.getOrderNo(), refundRecord.getId(), outRefundNo));
                    } else {
                        log.warn("退款已由微信确认，但订单当前状态不可退款，跳过支付记录退款标记和分账冲正: orderNo={}, refundNo={}, orderStatus={}",
                                order.getOrderNo(), outRefundNo, order.getStatus());
                    }
                }
            } else if ("ABNORMAL".equals(refundStatus) || "CLOSED".equals(refundStatus)) {
                refundRecordMapper.markRefundAbnormal(refundRecord.getId());
            }

            return buildSuccessResponse();
        } catch (Exception e) {
            log.error("处理微信退款回调异常", e);
            return buildErrorResponse("处理失败");
        }
    }

    private String readRequestBody(HttpServletRequest request) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private boolean validatePlatformPayNotify(MallOrder order, String mchId, String appId) {
        if (wxPayService == null || wxPayService.getConfig() == null
                || !StringUtils.equals(mchId, wxPayService.getConfig().getMchId())) {
            return false;
        }
        Merchant merchant = merchantService.selectMerchantById(order.getMerchantId());
        return merchant != null && StringUtils.equals(appId, merchant.getCMiniAppId());
    }

    private boolean validatePlatformMchId(String mchId) {
        return wxPayService != null && wxPayService.getConfig() != null
                && StringUtils.equals(mchId, wxPayService.getConfig().getMchId());
    }

    private String buildSuccessResponse() {
        return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
    }

    private String buildErrorResponse(String msg) {
        return "{\"code\":\"FAIL\",\"message\":\"" + msg + "\"}";
    }
}
