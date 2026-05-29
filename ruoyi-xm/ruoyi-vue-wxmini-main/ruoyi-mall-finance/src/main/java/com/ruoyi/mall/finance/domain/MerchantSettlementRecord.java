package com.ruoyi.mall.finance.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class MerchantSettlementRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String settlementNo;
    private Long merchantId;
    private Long distributorId;
    private Long storeId;
    private String orderNo;
    private String title;
    private BigDecimal orderAmount;
    private BigDecimal merchantAmount;
    private BigDecimal platformFeeAmount;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expectedTransferTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date transferTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date arriveTime;
    private String failReason;
    private String wechatBatchNo;
    private String wechatDetailNo;
    private Long reverseRecordId;
    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSettlementNo() { return settlementNo; }
    public void setSettlementNo(String settlementNo) { this.settlementNo = settlementNo; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    /** 商家名称（LEFT JOIN） */
    private String merchantName;
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public Long getDistributorId() { return distributorId; }
    public void setDistributorId(Long distributorId) { this.distributorId = distributorId; }

    /** 分销商名称（LEFT JOIN） */
    private String distributorName;
    public String getDistributorName() { return distributorName; }
    public void setDistributorName(String distributorName) { this.distributorName = distributorName; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }

    public BigDecimal getMerchantAmount() { return merchantAmount; }
    public void setMerchantAmount(BigDecimal merchantAmount) { this.merchantAmount = merchantAmount; }

    public BigDecimal getPlatformFeeAmount() { return platformFeeAmount; }
    public void setPlatformFeeAmount(BigDecimal platformFeeAmount) { this.platformFeeAmount = platformFeeAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getApplyTime() { return applyTime; }
    public void setApplyTime(Date applyTime) { this.applyTime = applyTime; }

    public Date getExpectedTransferTime() { return expectedTransferTime; }
    public void setExpectedTransferTime(Date expectedTransferTime) { this.expectedTransferTime = expectedTransferTime; }

    public Date getTransferTime() { return transferTime; }
    public void setTransferTime(Date transferTime) { this.transferTime = transferTime; }

    public Date getArriveTime() { return arriveTime; }
    public void setArriveTime(Date arriveTime) { this.arriveTime = arriveTime; }

    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }

    public String getWechatBatchNo() { return wechatBatchNo; }
    public void setWechatBatchNo(String wechatBatchNo) { this.wechatBatchNo = wechatBatchNo; }

    public String getWechatDetailNo() { return wechatDetailNo; }
    public void setWechatDetailNo(String wechatDetailNo) { this.wechatDetailNo = wechatDetailNo; }

    public Long getReverseRecordId() { return reverseRecordId; }
    public void setReverseRecordId(Long reverseRecordId) { this.reverseRecordId = reverseRecordId; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
