package com.ruoyi.mall.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;

public class WriteOffResultVO {
    private String orderNo;
    private String productName;
    private BigDecimal payAmount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date writeOffTime;
    private String operatorName;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }

    public Date getWriteOffTime() { return writeOffTime; }
    public void setWriteOffTime(Date writeOffTime) { this.writeOffTime = writeOffTime; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
}
