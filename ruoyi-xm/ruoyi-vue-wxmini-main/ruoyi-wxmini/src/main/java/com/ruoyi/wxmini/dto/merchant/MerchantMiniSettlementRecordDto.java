package com.ruoyi.wxmini.dto.merchant;

public class MerchantMiniSettlementRecordDto {

    private String settlementId;

    private String orderNo;

    private String title;

    private Long amount;

    private String status;

    private Long applyTime;

    private Long expectedTransferTime;

    private Long arriveTime;

    private String remark;

    public String getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(String settlementId) {
        this.settlementId = settlementId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Long applyTime) {
        this.applyTime = applyTime;
    }

    public Long getExpectedTransferTime() {
        return expectedTransferTime;
    }

    public void setExpectedTransferTime(Long expectedTransferTime) {
        this.expectedTransferTime = expectedTransferTime;
    }

    public Long getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(Long arriveTime) {
        this.arriveTime = arriveTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
