package com.ruoyi.mall.finance.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class DistributorSettlementRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String settlementNo;
    private Long distributorId;
    private Long merchantId;
    private String orderNo;
    private BigDecimal amount;
    private BigDecimal rate;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date settlementPeriodStart;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date settlementPeriodEnd;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expectedTransferTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date transferTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date arriveTime;
    private String failReason;
    private Long reverseRecordId;
    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSettlementNo() { return settlementNo; }
    public void setSettlementNo(String settlementNo) { this.settlementNo = settlementNo; }
    public Long getDistributorId() { return distributorId; }
    public void setDistributorId(Long distributorId) { this.distributorId = distributorId; }
    /** 分销商名称（LEFT JOIN） */
    private String distributorName;
    public String getDistributorName() { return distributorName; }
    public void setDistributorName(String distributorName) { this.distributorName = distributorName; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    /** 商家名称（LEFT JOIN） */
    private String merchantName;
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getSettlementPeriodStart() { return settlementPeriodStart; }
    public void setSettlementPeriodStart(Date settlementPeriodStart) { this.settlementPeriodStart = settlementPeriodStart; }
    public Date getSettlementPeriodEnd() { return settlementPeriodEnd; }
    public void setSettlementPeriodEnd(Date settlementPeriodEnd) { this.settlementPeriodEnd = settlementPeriodEnd; }
    public Date getExpectedTransferTime() { return expectedTransferTime; }
    public void setExpectedTransferTime(Date expectedTransferTime) { this.expectedTransferTime = expectedTransferTime; }
    public Date getTransferTime() { return transferTime; }
    public void setTransferTime(Date transferTime) { this.transferTime = transferTime; }
    public Date getArriveTime() { return arriveTime; }
    public void setArriveTime(Date arriveTime) { this.arriveTime = arriveTime; }
    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
    public Long getReverseRecordId() { return reverseRecordId; }
    public void setReverseRecordId(Long reverseRecordId) { this.reverseRecordId = reverseRecordId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
