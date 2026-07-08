package com.ruoyi.wxmini.dto.wx;

public class WxBookingRecordDto {

    private Long id;
    private String bookingNo;
    private Long merchantId;
    private Long productId;
    private String title;
    private String image;
    private Long price;
    private Long bookingTime;
    private String contactName;
    private String contactPhone;
    private Integer peopleCount;
    private String status;
    private String remark;
    private Long createTime;
    private Long confirmTime;
    private Long completeTime;
    private Long cancelTime;
    private Long expireTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBookingNo() { return bookingNo; }
    public void setBookingNo(String bookingNo) { this.bookingNo = bookingNo; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public Long getBookingTime() { return bookingTime; }
    public void setBookingTime(Long bookingTime) { this.bookingTime = bookingTime; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public Integer getPeopleCount() { return peopleCount; }
    public void setPeopleCount(Integer peopleCount) { this.peopleCount = peopleCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }
    public Long getConfirmTime() { return confirmTime; }
    public void setConfirmTime(Long confirmTime) { this.confirmTime = confirmTime; }
    public Long getCompleteTime() { return completeTime; }
    public void setCompleteTime(Long completeTime) { this.completeTime = completeTime; }
    public Long getCancelTime() { return cancelTime; }
    public void setCancelTime(Long cancelTime) { this.cancelTime = cancelTime; }
    public Long getExpireTime() { return expireTime; }
    public void setExpireTime(Long expireTime) { this.expireTime = expireTime; }
}
