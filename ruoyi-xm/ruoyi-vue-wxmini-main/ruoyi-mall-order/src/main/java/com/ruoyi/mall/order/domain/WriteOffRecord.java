package com.ruoyi.mall.order.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;

public class WriteOffRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private String orderNo;
    private String writeOffCode;
    private Long merchantId;
    private Long storeId;
    private Long operatorId;
    private String operatorName;
    /** 核销方式: 1扫码核销 2手动核销 */
    private Integer writeOffType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date writeOffTime;
    private String productName;
    private BigDecimal productAmount;
    private String remark;
    /** 状态: 1有效 0作废 */
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getWriteOffCode() { return writeOffCode; }
    public void setWriteOffCode(String writeOffCode) { this.writeOffCode = writeOffCode; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public Integer getWriteOffType() { return writeOffType; }
    public void setWriteOffType(Integer writeOffType) { this.writeOffType = writeOffType; }

    public Date getWriteOffTime() { return writeOffTime; }
    public void setWriteOffTime(Date writeOffTime) { this.writeOffTime = writeOffTime; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getProductAmount() { return productAmount; }
    public void setProductAmount(BigDecimal productAmount) { this.productAmount = productAmount; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
