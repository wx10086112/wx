package com.ruoyi.mall.order.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class RefundRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 退款状态常量 */
    public static final int STATUS_PENDING = 1;        // 待审核
    public static final int STATUS_APPROVED = 2;       // 已通过（审批通过，待微信退款）
    public static final int STATUS_REJECTED = 3;       // 已拒绝
    public static final int STATUS_REFUNDED = 4;       // 退款完成（微信确认）
    public static final int STATUS_ABNORMAL = 5;       // 退款异常

    private Long id;
    private String orderNo;
    private String refundNo;
    private Long merchantId;
    private Long userId;
    private Long paymentRecordId;
    private BigDecimal refundAmount;
    private String refundReason;
    private Integer refundType;
    private Integer status;
    private Date auditTime;
    private Date refundTime;
    private String rejectReason;
    private String operator;
    /** Number of failed WeChat refund attempts. */
    private Integer retryCount;
    /** Time of the latest failed WeChat refund attempt. */
    private Date lastRetryTime;
    /** Earliest time that the compensating task may retry this refund. */
    private Date nextRetryTime;
    /** Latest retry failure retained for operations review. */
    private String lastRetryReason;
    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag = "0";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getRefundNo() { return refundNo; }
    public void setRefundNo(String refundNo) { this.refundNo = refundNo; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    /** 商家名称（LEFT JOIN） */
    private String merchantName;
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    /** 用户昵称（LEFT JOIN） */
    private String userName;
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Long getPaymentRecordId() { return paymentRecordId; }
    public void setPaymentRecordId(Long paymentRecordId) { this.paymentRecordId = paymentRecordId; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }

    public Integer getRefundType() { return refundType; }
    public void setRefundType(Integer refundType) { this.refundType = refundType; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }

    public Date getRefundTime() { return refundTime; }
    public void setRefundTime(Date refundTime) { this.refundTime = refundTime; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public Date getLastRetryTime() { return lastRetryTime; }
    public void setLastRetryTime(Date lastRetryTime) { this.lastRetryTime = lastRetryTime; }

    public Date getNextRetryTime() { return nextRetryTime; }
    public void setNextRetryTime(Date nextRetryTime) { this.nextRetryTime = nextRetryTime; }

    public String getLastRetryReason() { return lastRetryReason; }
    public void setLastRetryReason(String lastRetryReason) { this.lastRetryReason = lastRetryReason; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
