package com.ruoyi.wxmini.dto.merchant;

public class MerchantMiniVerifyRecordDto {

    private Long recordId;

    private String orderNo;

    private Long goodsId;

    private String title;

    private String inputCode;

    private String writeOffCode;

    private String customerName;

    private String customerPhone;

    private Long payAmount;

    private String status;

    private String failureReason;

    private Long verifyTime;

    private Long verifyStaffId;

    private String verifyStaffName;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInputCode() {
        return inputCode;
    }

    public void setInputCode(String inputCode) {
        this.inputCode = inputCode;
    }

    public String getWriteOffCode() {
        return writeOffCode;
    }

    public void setWriteOffCode(String writeOffCode) {
        this.writeOffCode = writeOffCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Long getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(Long payAmount) {
        this.payAmount = payAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Long getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(Long verifyTime) {
        this.verifyTime = verifyTime;
    }

    public Long getVerifyStaffId() {
        return verifyStaffId;
    }

    public void setVerifyStaffId(Long verifyStaffId) {
        this.verifyStaffId = verifyStaffId;
    }

    public String getVerifyStaffName() {
        return verifyStaffName;
    }

    public void setVerifyStaffName(String verifyStaffName) {
        this.verifyStaffName = verifyStaffName;
    }
}
