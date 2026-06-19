package com.ruoyi.mall.order.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

public class MallOrderStatusHistory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private String orderNo;
    private Long merchantId;
    private Long userId;
    private Integer fromStatus;
    private Integer toStatus;
    private String action;
    private String source;
    private Long operatorId;
    private String operatorName;
    private String remark;
    private Date changeTime;
    private String delFlag = "0";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getFromStatus() { return fromStatus; }
    public void setFromStatus(Integer fromStatus) { this.fromStatus = fromStatus; }
    public Integer getToStatus() { return toStatus; }
    public void setToStatus(Integer toStatus) { this.toStatus = toStatus; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getChangeTime() { return changeTime; }
    public void setChangeTime(Date changeTime) { this.changeTime = changeTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
