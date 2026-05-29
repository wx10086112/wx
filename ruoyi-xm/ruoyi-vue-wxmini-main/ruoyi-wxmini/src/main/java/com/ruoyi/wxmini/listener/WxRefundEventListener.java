package com.ruoyi.wxmini.listener;

import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ruoyi.mall.common.event.RefundApprovedEvent;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 微信退款事件监听器
 * 监听 RefundApprovedEvent，调用微信退款V3 API
 */
@Component
public class WxRefundEventListener {

    private static final Logger log = LoggerFactory.getLogger(WxRefundEventListener.class);

    @Value("${wx.pay.refund-notify-url:https://xxx.com/api/wxmini/pay/refund-notify}")
    private String refundNotifyUrl;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private WxPayService wxPayService;
    @Resource
    private RefundRecordMapper refundRecordMapper;
    @Resource
    private MallOrderMapper mallOrderMapper;

    @EventListener
    @Async
    public void onRefundApproved(RefundApprovedEvent event) {
        if (wxPayService == null) {
            log.info("WxPayService未配置，跳过微信退款（stub模式）: orderNo={}", event.getOrderNo());
            return;
        }

        String orderNo = event.getOrderNo();
        Long refundRecordId = event.getRefundRecordId();
        log.info("收到退款审批事件，准备调用微信退款API: orderNo={}, refundId={}", orderNo, refundRecordId);

        try {
            // 查询退款记录
            RefundRecord refundRecord = refundRecordMapper.selectRefundRecordById(refundRecordId);
            if (refundRecord == null) {
                log.error("退款记录不存在: refundId={}", refundRecordId);
                return;
            }

            // 查询原订单
            MallOrder order = mallOrderMapper.selectMallOrderByOrderNo(orderNo);
            if (order == null) {
                log.error("原订单不存在: orderNo={}", orderNo);
                return;
            }

            // 构建退款请求
            WxPayRefundV3Request request = new WxPayRefundV3Request();
            request.setOutTradeNo(orderNo);
            request.setOutRefundNo(refundRecord.getRefundNo() != null ? refundRecord.getRefundNo() : "RF_" + orderNo);
            request.setReason(refundRecord.getRefundReason() != null ? refundRecord.getRefundReason() : "用户申请退款");

            WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
            // 退款金额（分）= 原订单金额全额退款
            int refundAmountFen = order.getPayAmount().multiply(BigDecimal.valueOf(100)).intValue();
            amount.setRefund(refundAmountFen);
            amount.setTotal(refundAmountFen);
            amount.setCurrency("CNY");
            request.setAmount(amount);

            request.setNotifyUrl(refundNotifyUrl);

            // 调用微信退款API
            WxPayRefundV3Result result = wxPayService.refundV3(request);
            log.info("微信退款API调用成功: orderNo={}, refundNo={}, status={}",
                    orderNo, result.getOutRefundNo(), result.getStatus());

            // 更新退款记录：记录微信退款单号
            if (result.getRefundId() != null) {
                refundRecord.setRefundNo(result.getOutRefundNo());
                refundRecordMapper.updateRefundRecord(refundRecord);
            }
        } catch (Exception e) {
            log.error("微信退款API调用失败: orderNo={}, error={}", orderNo, e.getMessage(), e);
        }
    }
}
