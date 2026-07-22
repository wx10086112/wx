package com.ruoyi.wxmini.dto.merchant;

public class MerchantMiniWorkbenchStatsDto {

    private Integer pendingAcceptCount;

    private Integer pendingVerifyCount;

    private Integer completedCount;

    private Integer refundingCount;

    private Integer onShelfCount;

    private Integer pendingBookingCount;

    private Integer todayOrderCount;

    private Long todaySalesAmount;

    public Integer getPendingAcceptCount() {
        return pendingAcceptCount;
    }

    public void setPendingAcceptCount(Integer pendingAcceptCount) {
        this.pendingAcceptCount = pendingAcceptCount;
    }

    public Integer getPendingVerifyCount() {
        return pendingVerifyCount;
    }

    public void setPendingVerifyCount(Integer pendingVerifyCount) {
        this.pendingVerifyCount = pendingVerifyCount;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Integer getRefundingCount() {
        return refundingCount;
    }

    public void setRefundingCount(Integer refundingCount) {
        this.refundingCount = refundingCount;
    }

    public Integer getOnShelfCount() {
        return onShelfCount;
    }

    public void setOnShelfCount(Integer onShelfCount) {
        this.onShelfCount = onShelfCount;
    }

    public Integer getPendingBookingCount() {
        return pendingBookingCount;
    }

    public void setPendingBookingCount(Integer pendingBookingCount) {
        this.pendingBookingCount = pendingBookingCount;
    }

    public Integer getTodayOrderCount() {
        return todayOrderCount;
    }

    public void setTodayOrderCount(Integer todayOrderCount) {
        this.todayOrderCount = todayOrderCount;
    }

    public Long getTodaySalesAmount() {
        return todaySalesAmount;
    }

    public void setTodaySalesAmount(Long todaySalesAmount) {
        this.todaySalesAmount = todaySalesAmount;
    }
}
