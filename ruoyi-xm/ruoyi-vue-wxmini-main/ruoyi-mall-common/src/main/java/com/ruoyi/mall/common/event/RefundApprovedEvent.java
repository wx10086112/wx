package com.ruoyi.mall.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * 退款审批通过事件
 * 由订单模块发布，财务模块监听并处理结算逆向
 */
public class RefundApprovedEvent extends ApplicationEvent {

    private final String orderNo;
    private final Long refundRecordId;
    private final String operator;

    public RefundApprovedEvent(Object source, String orderNo, Long refundRecordId, String operator) {
        super(source);
        this.orderNo = orderNo;
        this.refundRecordId = refundRecordId;
        this.operator = operator;
    }

    public String getOrderNo() { return orderNo; }
    public Long getRefundRecordId() { return refundRecordId; }
    public String getOperator() { return operator; }
}
