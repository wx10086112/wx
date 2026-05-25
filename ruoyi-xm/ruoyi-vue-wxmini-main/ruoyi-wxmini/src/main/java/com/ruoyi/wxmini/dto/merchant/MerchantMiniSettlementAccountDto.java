package com.ruoyi.wxmini.dto.merchant;

public class MerchantMiniSettlementAccountDto {

    private String accountName;

    private String bankName;

    private String accountNoTail;

    private String status;

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNoTail() {
        return accountNoTail;
    }

    public void setAccountNoTail(String accountNoTail) {
        this.accountNoTail = accountNoTail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
