package com.ruoyi.wxmini.dto.merchant;

public class MerchantMiniDailyFlowRecordDto {

    private Long id;

    private String orderNo;

    private String title;

    private String type;

    private Long orderAmount;

    private Long merchantAmount;

    private Long platformFeeAmount;

    private String status;

    private Long flowTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Long getFlowTime() {
        return flowTime;
    }

    public void setFlowTime(Long flowTime) {
        this.flowTime = flowTime;
    }
}
