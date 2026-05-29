package com.ruoyi.mall.finance.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class PlatformTransferRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String transferNo;
    private String settlementNo;
    private String targetType;
    private Long targetId;
    private Long merchantId;
    private Long distributorId;
    private String orderNo;
    private BigDecimal amount;
    private String receiverOpenid;
    private String receiverName;
    private String receiverAccountType;
    private String wechatBatchNo;
    private String wechatDetailNo;
    private String status;
    private String failReason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date transferTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date arriveTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date notifyTime;
    private String notifyResult;
    private String operatorId;
    private String delFlag;

    /** 商家名称（LEFT JOIN） */
    private String merchantName;
    /** 分销商名称（LEFT JOIN） */
    private String distributorName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransferNo() { return transferNo; }
    public void setTransferNo(String transferNo) { this.transferNo = transferNo; }

    public String getSettlementNo() { return settlementNo; }
    public void setSettlementNo(String settlementNo) { this.settlementNo = settlementNo; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public Long getDistributorId() { return distributorId; }
    public void setDistributorId(Long distributorId) { this.distributorId = distributorId; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReceiverOpenid() { return receiverOpenid; }
    public void setReceiverOpenid(String receiverOpenid) { this.receiverOpenid = receiverOpenid; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverAccountType() { return receiverAccountType; }
    public void setReceiverAccountType(String receiverAccountType) { this.receiverAccountType = receiverAccountType; }

    public String getWechatBatchNo() { return wechatBatchNo; }
    public void setWechatBatchNo(String wechatBatchNo) { this.wechatBatchNo = wechatBatchNo; }

    public String getWechatDetailNo() { return wechatDetailNo; }
    public void setWechatDetailNo(String wechatDetailNo) { this.wechatDetailNo = wechatDetailNo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }

    public Date getApplyTime() { return applyTime; }
    public void setApplyTime(Date applyTime) { this.applyTime = applyTime; }

    public Date getTransferTime() { return transferTime; }
    public void setTransferTime(Date transferTime) { this.transferTime = transferTime; }

    public Date getArriveTime() { return arriveTime; }
    public void setArriveTime(Date arriveTime) { this.arriveTime = arriveTime; }

    public Date getNotifyTime() { return notifyTime; }
    public void setNotifyTime(Date notifyTime) { this.notifyTime = notifyTime; }

    public String getNotifyResult() { return notifyResult; }
    public void setNotifyResult(String notifyResult) { this.notifyResult = notifyResult; }

    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public String getDistributorName() { return distributorName; }
    public void setDistributorName(String distributorName) { this.distributorName = distributorName; }
}
