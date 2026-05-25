package com.ruoyi.wxmini.dto.merchant;

public class MerchantMiniWorkbenchStatsDto {

    private Integer pendingAcceptCount;

    private Integer pendingVerifyCount;

    private Integer completedCount;

    private Integer refundingCount;

    private Integer onShelfCount;

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

    public Long getTodaySalesAmount() {
        return todaySalesAmount;
    }

    public void setTodaySalesAmount(Long todaySalesAmount) {
        this.todaySalesAmount = todaySalesAmount;
    }
}
