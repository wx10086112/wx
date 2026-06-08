package com.ruoyi.mall.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * 微信确认退款成功事件。
 * 只有收到微信退款成功回调后才发布，避免审批通过但资金未退回时提前冲回结算。
 */
public class RefundSucceededEvent extends ApplicationEvent {

    private final String orderNo;
    private final Long refundRecordId;
    private final String refundNo;

    public RefundSucceededEvent(Object source, String orderNo, Long refundRecordId, String refundNo) {
        super(source);
        this.orderNo = orderNo;
        this.refundRecordId = refundRecordId;
        this.refundNo = refundNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Long getRefundRecordId() {
        return refundRecordId;
    }

    public String getRefundNo() {
        return refundNo;
    }
}
