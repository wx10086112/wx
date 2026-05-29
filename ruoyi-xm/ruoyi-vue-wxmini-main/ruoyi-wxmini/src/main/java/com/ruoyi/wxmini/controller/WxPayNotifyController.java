package com.ruoyi.wxmini.controller;

import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyV3Result;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import com.ruoyi.mall.order.service.IMallOrderService;
import com.ruoyi.mall.pay.service.IPaymentRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.Date;

/**
 * 微信支付回调Controller
 * 白名单路径: /wxmini/pay/notify
 */
@RestController
@RequestMapping("/wxmini/pay")
public class WxPayNotifyController {

    private static final Logger log = LoggerFactory.getLogger(WxPayNotifyController.class);

    private static final int ORDER_STATUS_PENDING = 0;
    private static final int ORDER_STATUS_PAID = 1;

    @Autowired(required = false)
    private WxPayService wxPayService;
    @Autowired
    private IMallOrderService mallOrderService;
    @Autowired
    private IPaymentRecordService paymentRecordService;
    @Autowired
    private RefundRecordMapper refundRecordMapper;

    /**
     * 微信支付V3回调（JSON格式）
     * POST /wxmini/pay/notify
     */
    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) {
        try {
            if (wxPayService == null) {
                log.warn("WxPayService未配置，忽略支付回调");
                return buildSuccessResponse();
            }

            String body = readRequestBody(request);
            log.info("收到微信支付回调: {}", body);

            SignatureHeader header = new SignatureHeader();
            header.setTimeStamp(request.getHeader("Wechatpay-Timestamp"));
            header.setNonce(request.getHeader("Wechatpay-Nonce"));
            header.setSignature(request.getHeader("Wechatpay-Signature"));
            header.setSerial(request.getHeader("Wechatpay-Serial"));

            WxPayNotifyV3Result result = wxPayService.parseOrderNotifyV3Result(body, header);
            String outTradeNo = result.getResult().getOutTradeNo();
            String transactionId = result.getResult().getTransactionId();
            String tradeState = result.getResult().getTradeState();

            log.info("支付回调解析成功: outTradeNo={}, transactionId={}, tradeState={}", outTradeNo, transactionId, tradeState);

            MallOrder order = mallOrderService.selectMallOrderByOrderNo(outTradeNo);
            if (order == null) {
                log.error("支付回调订单不存在: {}", outTradeNo);
                return buildSuccessResponse();
            }

            // 幂等：已支付订单跳过
            if (order.getStatus() != null && order.getStatus() >= ORDER_STATUS_PAID) {
                log.info("订单{}已支付，幂等跳过", outTradeNo);
                return buildSuccessResponse();
            }

            if ("SUCCESS".equals(tradeState)) {
                order.setStatus(ORDER_STATUS_PAID);
                order.setPayTime(new Date());
                mallOrderService.updateMallOrder(order);

                // 更新支付记录（全链路留痕：transactionId 持久化）
                paymentRecordService.markPaySuccess(outTradeNo, transactionId, body);

                log.info("订单{}支付成功，已更新状态（transactionId={}）", outTradeNo, transactionId);
            } else {
                log.warn("订单{}支付状态非SUCCESS: {}", outTradeNo, tradeState);
            }

            return buildSuccessResponse();
        } catch (Exception e) {
            log.error("处理微信支付回调异常", e);
            return buildErrorResponse("处理失败");
        }
    }

    /**
     * 微信退款回调
     * POST /wxmini/pay/refund-notify
     */
    @PostMapping("/refund-notify")
    public String refundNotify(HttpServletRequest request) {
        try {
            if (wxPayService == null) {
                log.warn("WxPayService未配置，忽略退款回调");
                return buildSuccessResponse();
            }

            String body = readRequestBody(request);
            log.info("收到微信退款回调: {}", body);

            SignatureHeader header = new SignatureHeader();
            header.setTimeStamp(request.getHeader("Wechatpay-Timestamp"));
            header.setNonce(request.getHeader("Wechatpay-Nonce"));
            header.setSignature(request.getHeader("Wechatpay-Signature"));
            header.setSerial(request.getHeader("Wechatpay-Serial"));

            WxPayRefundNotifyV3Result result = wxPayService.parseRefundNotifyV3Result(body, header);
            String outRefundNo = result.getResult().getOutRefundNo();
            String refundStatus = result.getResult().getRefundStatus();

            log.info("退款回调解析成功: outRefundNo={}, refundStatus={}", outRefundNo, refundStatus);

            if ("SUCCESS".equals(refundStatus)) {
                log.info("退款{}成功，更新退款记录状态", outRefundNo);
                RefundRecord refundRecord = refundRecordMapper.selectRefundRecordByRefundNo(outRefundNo);
                if (refundRecord != null && refundRecord.getStatus() != null && refundRecord.getStatus() < RefundRecord.STATUS_REFUNDED) {
                    refundRecord.setStatus(RefundRecord.STATUS_REFUNDED);
                    refundRecord.setRefundTime(new java.util.Date());
                    refundRecordMapper.updateRefundRecord(refundRecord);
                    log.info("退款记录 {} 已更新为退款完成", refundRecord.getId());
                } else if (refundRecord == null) {
                    log.warn("退款回调: 未找到退款记录 refundNo={}", outRefundNo);
                }
            } else if ("ABNORMAL".equals(refundStatus) || "CLOSED".equals(refundStatus)) {
                log.warn("退款{}异常: {}", outRefundNo, refundStatus);
                RefundRecord refundRecord = refundRecordMapper.selectRefundRecordByRefundNo(outRefundNo);
                if (refundRecord != null) {
                    refundRecord.setStatus(RefundRecord.STATUS_ABNORMAL);
                    refundRecordMapper.updateRefundRecord(refundRecord);
                }
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

    private String buildSuccessResponse() {
        return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
    }

    private String buildErrorResponse(String msg) {
        return "{\"code\":\"FAIL\",\"message\":\"" + msg + "\"}";
    }
}
