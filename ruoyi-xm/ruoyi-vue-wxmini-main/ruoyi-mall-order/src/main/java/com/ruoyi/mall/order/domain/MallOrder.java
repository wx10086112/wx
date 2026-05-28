package com.ruoyi.mall.order.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class MallOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private Long merchantId;
    private Long distributorId;
    private Long userId;
    private Long storeId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal commission;
    private BigDecimal merchantIncome;
    private Long couponId;
    private BigDecimal couponAmount;
    private Long grouponId;
    private Integer status;
    private String writeOffCode;
    /** 核销状态: 0未核销 1已核销 */
    private Integer writeOffStatus;
    private Date writeOffTime;
    private Long writeOffUserId;
    /** 商品有效天数(冗余自商品表) */
    private Integer validDays;
    private Date payTime;
    private Date useTime;
    private Date completeTime;
    private Date cancelTime;
    private Date refundTime;
    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag = "0";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public Long getDistributorId() { return distributorId; }
    public void setDistributorId(Long distributorId) { this.distributorId = distributorId; }

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

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }

    public BigDecimal getCommission() { return commission; }
    public void setCommission(BigDecimal commission) { this.commission = commission; }

    public BigDecimal getMerchantIncome() { return merchantIncome; }
    public void setMerchantIncome(BigDecimal merchantIncome) { this.merchantIncome = merchantIncome; }

    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }

    public BigDecimal getCouponAmount() { return couponAmount; }
    public void setCouponAmount(BigDecimal couponAmount) { this.couponAmount = couponAmount; }

    public Long getGrouponId() { return grouponId; }
    public void setGrouponId(Long grouponId) { this.grouponId = grouponId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getWriteOffCode() { return writeOffCode; }
    public void setWriteOffCode(String writeOffCode) { this.writeOffCode = writeOffCode; }

    public Integer getWriteOffStatus() { return writeOffStatus; }
    public void setWriteOffStatus(Integer writeOffStatus) { this.writeOffStatus = writeOffStatus; }

    public Date getWriteOffTime() { return writeOffTime; }
    public void setWriteOffTime(Date writeOffTime) { this.writeOffTime = writeOffTime; }

    public Long getWriteOffUserId() { return writeOffUserId; }
    public void setWriteOffUserId(Long writeOffUserId) { this.writeOffUserId = writeOffUserId; }

    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }

    public Date getPayTime() { return payTime; }
    public void setPayTime(Date payTime) { this.payTime = payTime; }

    public Date getUseTime() { return useTime; }
    public void setUseTime(Date useTime) { this.useTime = useTime; }

    public Date getCompleteTime() { return completeTime; }
    public void setCompleteTime(Date completeTime) { this.completeTime = completeTime; }

    public Date getCancelTime() { return cancelTime; }
    public void setCancelTime(Date cancelTime) { this.cancelTime = cancelTime; }

    public Date getRefundTime() { return refundTime; }
    public void setRefundTime(Date refundTime) { this.refundTime = refundTime; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
