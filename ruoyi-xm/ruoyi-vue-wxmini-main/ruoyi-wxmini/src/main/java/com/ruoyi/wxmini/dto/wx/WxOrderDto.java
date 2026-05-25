package com.ruoyi.wxmini.dto.wx;

public class WxOrderDto {

    private Long id;
    private String orderNo;
    private Long productId;
    private Long merchantId;
    private String title;
    private String merchantName;
    private String image;
    private Integer quantity;
    private Long orderAmount;
    private Long couponAmount;
    private Long payAmount;
    private Long price;
    private String phone;
    private String status;
    private Long createTime;
    private Long payTime;
    private String writeOffCode;
    private Long writeOffDeadline;
    private Long writeOffTime;
    private String refundReason;
    private Long refundTime;
    private Long expireTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Long getOrderAmount() { return orderAmount; }
    public void setOrderAmount(Long orderAmount) { this.orderAmount = orderAmount; }
    public Long getCouponAmount() { return couponAmount; }
    public void setCouponAmount(Long couponAmount) { this.couponAmount = couponAmount; }
    public Long getPayAmount() { return payAmount; }
    public void setPayAmount(Long payAmount) { this.payAmount = payAmount; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }
    public Long getPayTime() { return payTime; }
    public void setPayTime(Long payTime) { this.payTime = payTime; }
    public String getWriteOffCode() { return writeOffCode; }
    public void setWriteOffCode(String writeOffCode) { this.writeOffCode = writeOffCode; }
    public Long getWriteOffDeadline() { return writeOffDeadline; }
    public void setWriteOffDeadline(Long writeOffDeadline) { this.writeOffDeadline = writeOffDeadline; }
    public Long getWriteOffTime() { return writeOffTime; }
    public void setWriteOffTime(Long writeOffTime) { this.writeOffTime = writeOffTime; }
    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }
    public Long getRefundTime() { return refundTime; }
    public void setRefundTime(Long refundTime) { this.refundTime = refundTime; }
    public Long getExpireTime() { return expireTime; }
    public void setExpireTime(Long expireTime) { this.expireTime = expireTime; }
}
