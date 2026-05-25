package com.ruoyi.wxmini.dto.merchant;

import java.util.List;

public class MerchantMiniSettlementOverviewDto {

    private Long todayIncomeAmount;

    private Long monthIncomeAmount;

    private Long pendingSettleAmount;

    private Long settledAmount;

    private Long processingAmount;

    private Long pendingAutoTransferAmount;

    private Long platformFeeAmount;

    private Integer completedOrderCount;

    private String autoTransferMode;

    private Long nextAutoTransferTime;

    private MerchantMiniSettlementAccountDto settlementAccount;

    private List<MerchantMiniSettlementRecordDto> settlementRecordList;

    private List<MerchantMiniFinanceLedgerDto> ledgerList;

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

    public Long getSettledAmount() {
        return settledAmount;
    }

    public void setSettledAmount(Long settledAmount) {
        this.settledAmount = settledAmount;
    }

    public Long getProcessingAmount() {
        return processingAmount;
    }

    public void setProcessingAmount(Long processingAmount) {
        this.processingAmount = processingAmount;
    }

    public Long getPendingAutoTransferAmount() {
        return pendingAutoTransferAmount;
    }

    public void setPendingAutoTransferAmount(Long pendingAutoTransferAmount) {
        this.pendingAutoTransferAmount = pendingAutoTransferAmount;
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

    public String getAutoTransferMode() {
        return autoTransferMode;
    }

    public void setAutoTransferMode(String autoTransferMode) {
        this.autoTransferMode = autoTransferMode;
    }

    public Long getNextAutoTransferTime() {
        return nextAutoTransferTime;
    }

    public void setNextAutoTransferTime(Long nextAutoTransferTime) {
        this.nextAutoTransferTime = nextAutoTransferTime;
    }

    public MerchantMiniSettlementAccountDto getSettlementAccount() {
        return settlementAccount;
    }

    public void setSettlementAccount(MerchantMiniSettlementAccountDto settlementAccount) {
        this.settlementAccount = settlementAccount;
    }

    public List<MerchantMiniSettlementRecordDto> getSettlementRecordList() {
        return settlementRecordList;
    }

    public void setSettlementRecordList(List<MerchantMiniSettlementRecordDto> settlementRecordList) {
        this.settlementRecordList = settlementRecordList;
    }

    public List<MerchantMiniFinanceLedgerDto> getLedgerList() {
        return ledgerList;
    }

    public void setLedgerList(List<MerchantMiniFinanceLedgerDto> ledgerList) {
        this.ledgerList = ledgerList;
    }
}
