package com.ruoyi.wxmini.dto.wx;

import javax.validation.constraints.NotNull;

public class WxBookingCreateRequestDto {

    @NotNull(message = "商品不能为空")
    private Long productId;
    @NotNull(message = "预约时间不能为空")
    private Long bookingTime;
    private String contactName;
    private String contactPhone;
    private Integer peopleCount;
    private String remark;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getBookingTime() { return bookingTime; }
    public void setBookingTime(Long bookingTime) { this.bookingTime = bookingTime; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public Integer getPeopleCount() { return peopleCount; }
    public void setPeopleCount(Integer peopleCount) { this.peopleCount = peopleCount; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
