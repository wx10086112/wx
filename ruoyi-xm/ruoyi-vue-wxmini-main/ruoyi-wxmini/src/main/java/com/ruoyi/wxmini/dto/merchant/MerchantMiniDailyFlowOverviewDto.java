package com.ruoyi.wxmini.dto.merchant;

import java.util.List;

public class MerchantMiniDailyFlowOverviewDto {

    private String range;

    private String startDate;

    private String endDate;

    private Long totalAmount;

    private Long merchantAmount;

    private Long platformFeeAmount;

    private Long refundAmount;

    private Integer orderCount;

    private List<MerchantMiniDailyFlowDayDto> dailyList;

    private List<MerchantMiniDailyFlowRecordDto> recordList;

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
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

    public Long getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Long refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public List<MerchantMiniDailyFlowDayDto> getDailyList() {
        return dailyList;
    }

    public void setDailyList(List<MerchantMiniDailyFlowDayDto> dailyList) {
        this.dailyList = dailyList;
    }

    public List<MerchantMiniDailyFlowRecordDto> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<MerchantMiniDailyFlowRecordDto> recordList) {
        this.recordList = recordList;
    }
}
