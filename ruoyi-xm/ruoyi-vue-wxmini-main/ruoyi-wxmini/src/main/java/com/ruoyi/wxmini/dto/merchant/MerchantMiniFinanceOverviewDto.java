package com.ruoyi.wxmini.dto.merchant;

import java.util.List;

public class MerchantMiniFinanceOverviewDto {

    private Long todayIncomeAmount;

    private Long monthIncomeAmount;

    private Long pendingSettleAmount;

    private Long withdrawableAmount;

    private Long platformFeeAmount;

    private Integer completedOrderCount;

    private List<MerchantMiniFinanceLedgerDto> ledgerList;

    private List<MerchantMiniWithdrawRecordDto> withdrawList;

    public Long getTodayIncomeAmount() {
        return todayIncomeAmount;
    }

    public void setTodayIncomeAmount(Long todayIncomeAmount) {
        this.todayIncomeAmount = todayIncomeAmount;
    }

    public Long getMonthIncomeAmount() {
        return monthIncomeAmount;
    }

    public void setMonthIncomeAmount(Long monthIncomeAmount) {
        this.monthIncomeAmount = monthIncomeAmount;
    }

    public Long getPendingSettleAmount() {
        return pendingSettleAmount;
    }

    public void setPendingSettleAmount(Long pendingSettleAmount) {
        this.pendingSettleAmount = pendingSettleAmount;
    }

    public Long getWithdrawableAmount() {
        return withdrawableAmount;
    }

    public void setWithdrawableAmount(Long withdrawableAmount) {
        this.withdrawableAmount = withdrawableAmount;
    }

    public Long getPlatformFeeAmount() {
        return platformFeeAmount;
    }

    public void setPlatformFeeAmount(Long platformFeeAmount) {
        this.platformFeeAmount = platformFeeAmount;
    }

    public Integer getCompletedOrderCount() {
        return completedOrderCount;
    }

    public void setCompletedOrderCount(Integer completedOrderCount) {
        this.completedOrderCount = completedOrderCount;
    }

    public List<MerchantMiniFinanceLedgerDto> getLedgerList() {
        return ledgerList;
    }

    public void setLedgerList(List<MerchantMiniFinanceLedgerDto> ledgerList) {
        this.ledgerList = ledgerList;
    }

    public List<MerchantMiniWithdrawRecordDto> getWithdrawList() {
        return withdrawList;
    }

    public void setWithdrawList(List<MerchantMiniWithdrawRecordDto> withdrawList) {
        this.withdrawList = withdrawList;
    }
}
