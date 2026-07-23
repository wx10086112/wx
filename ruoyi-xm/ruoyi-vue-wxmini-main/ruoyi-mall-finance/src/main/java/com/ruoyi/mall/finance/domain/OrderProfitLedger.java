package com.ruoyi.mall.finance.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class OrderProfitLedger extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private Long merchantId;
    private Long distributorId;
    private BigDecimal payAmount;
    private BigDecimal merchantAmount;
    private BigDecimal platformAmount;
    private BigDecimal distributorAmount;
    private BigDecimal merchantRate;
    private BigDecimal platformRate;
    private BigDecimal distributorRate;
    private String status;
    private Integer profitSharingAttempts;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date profitSharingLastAttemptTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date profitSharingNextRetryTime;
    private String profitSharingOutOrderNo;
    private String profitSharingOrderId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;
    private String remark;
    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
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
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public BigDecimal getMerchantAmount() { return merchantAmount; }
    public void setMerchantAmount(BigDecimal merchantAmount) { this.merchantAmount = merchantAmount; }
    public BigDecimal getPlatformAmount() { return platformAmount; }
    public void setPlatformAmount(BigDecimal platformAmount) { this.platformAmount = platformAmount; }
    public BigDecimal getDistributorAmount() { return distributorAmount; }
    public void setDistributorAmount(BigDecimal distributorAmount) { this.distributorAmount = distributorAmount; }
    public BigDecimal getMerchantRate() { return merchantRate; }
    public void setMerchantRate(BigDecimal merchantRate) { this.merchantRate = merchantRate; }
    public BigDecimal getPlatformRate() { return platformRate; }
    public void setPlatformRate(BigDecimal platformRate) { this.platformRate = platformRate; }
    public BigDecimal getDistributorRate() { return distributorRate; }
    public void setDistributorRate(BigDecimal distributorRate) { this.distributorRate = distributorRate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getProfitSharingAttempts() { return profitSharingAttempts; }
    public void setProfitSharingAttempts(Integer profitSharingAttempts) { this.profitSharingAttempts = profitSharingAttempts; }
    public Date getProfitSharingLastAttemptTime() { return profitSharingLastAttemptTime; }
    public void setProfitSharingLastAttemptTime(Date profitSharingLastAttemptTime) { this.profitSharingLastAttemptTime = profitSharingLastAttemptTime; }
    public Date getProfitSharingNextRetryTime() { return profitSharingNextRetryTime; }
    public void setProfitSharingNextRetryTime(Date profitSharingNextRetryTime) { this.profitSharingNextRetryTime = profitSharingNextRetryTime; }
    public String getProfitSharingOutOrderNo() { return profitSharingOutOrderNo; }
    public void setProfitSharingOutOrderNo(String profitSharingOutOrderNo) { this.profitSharingOutOrderNo = profitSharingOutOrderNo; }
    public String getProfitSharingOrderId() { return profitSharingOrderId; }
    public void setProfitSharingOrderId(String profitSharingOrderId) { this.profitSharingOrderId = profitSharingOrderId; }
    public Date getFinishTime() { return finishTime; }
    public void setFinishTime(Date finishTime) { this.finishTime = finishTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
