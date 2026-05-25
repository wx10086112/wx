package com.ruoyi.wxmini.dto.merchant;

public class MerchantMiniFinanceLedgerDto {

    private Long ledgerId;

    private String orderNo;

    private String title;

    private Long orderAmount;

    private Long merchantAmount;

    private Long platformFeeAmount;

    private String status;

    private Long finishTime;

    private Long settleTime;

    public Long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId) {
        this.ledgerId = ledgerId;
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

    public Long getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(Long orderAmount) {
        this.orderAmount = orderAmount;
    }

    public Long getMerchantAmount() {
        return merchantAmount;
    }

    public void setMerchantAmount(Long merchantAmount) {
        this.merchantAmount = merchantAmount;
    }

    public Long getPlatformFeeAmount() {
        return platformFeeAmount;
    }

    public void setPlatformFeeAmount(Long platformFeeAmount) {
        this.platformFeeAmount = platformFeeAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Long finishTime) {
        this.finishTime = finishTime;
    }

    public Long getSettleTime() {
        return settleTime;
    }

    public void setSettleTime(Long settleTime) {
        this.settleTime = settleTime;
    }
}
