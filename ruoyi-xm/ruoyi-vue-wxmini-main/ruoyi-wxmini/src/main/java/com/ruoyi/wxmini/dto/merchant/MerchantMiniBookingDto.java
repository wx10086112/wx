package com.ruoyi.wxmini.dto.merchant;

public class MerchantMiniBookingDto {

    private Long id;
    private String bookingNo;
    private Long goodsId;
    private String title;
    private String image;
    private Long price;
    private String customerName;
    private String customerPhone;
    private Integer peopleCount;
    private String status;
    private Long bookingTime;
    private Long createTime;
    private Long confirmTime;
    private Long completeTime;
    private Long cancelTime;
    private Long expireTime;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBookingNo() { return bookingNo; }
    public void setBookingNo(String bookingNo) { this.bookingNo = bookingNo; }
    public Long getGoodsId() { return goodsId; }
    public void setGoodsId(Long goodsId) { this.goodsId = goodsId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public Integer getPeopleCount() { return peopleCount; }
    public void setPeopleCount(Integer peopleCount) { this.peopleCount = peopleCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getBookingTime() { return bookingTime; }
    public void setBookingTime(Long bookingTime) { this.bookingTime = bookingTime; }
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
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
