package com.ruoyi.mall.order.event;

import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * 订单完成事件，用于触发结算记录生成
 */
public class OrderCompletedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private final String orderNo;
    private final Long merchantId;
    private final Long distributorId;
    private final Long storeId;
    private final BigDecimal payAmount;
    private final String title;

    public OrderCompletedEvent(Object source, String orderNo, Long merchantId, Long distributorId, Long storeId, BigDecimal payAmount, String title) {
        super(source);
        this.orderNo = orderNo;
        this.merchantId = merchantId;
        this.distributorId = distributorId;
        this.storeId = storeId;
        this.payAmount = payAmount;
        this.title = title;
    }

    public String getOrderNo() { return orderNo; }
    public Long getMerchantId() { return merchantId; }
    public Long getDistributorId() { return distributorId; }
    public Long getStoreId() { return storeId; }
    public BigDecimal getPayAmount() { return payAmount; }
    public String getTitle() { return title; }
}
