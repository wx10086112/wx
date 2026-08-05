package com.ruoyi.wxmini.dto.merchant;

public class MerchantMiniEntryInfoDto {

    private Long merchantId;
    private String merchantName;
    private String merchantImage;
    private String contact;
    private String phone;
    private String loginPage;
    private String entryAppId;
    private boolean miniAppConfigured;

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantImage() {
        return merchantImage;
    }

    public void setMerchantImage(String merchantImage) {
        this.merchantImage = merchantImage;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLoginPage() {
        return loginPage;
    }

    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
    }

    public String getEntryAppId() {
        return entryAppId;
    }

    public void setEntryAppId(String entryAppId) {
        this.entryAppId = entryAppId;
    }

    public boolean isMiniAppConfigured() {
        return miniAppConfigured;
    }

    public void setMiniAppConfigured(boolean miniAppConfigured) {
        this.miniAppConfigured = miniAppConfigured;
    }
}
