package com.ruoyi.wxmini.dto.merchant;

import java.util.List;

public class MerchantMiniStoreDto {

    private Long merchantId;

    private Long storeId;

    private String storeName;

    private String brandSlogan;

    private String notice;

    private String businessHours;

    private String phone;

    private String address;

    private List<String> serviceTags;

    private List<String> bannerTitles;

    private Boolean businessStatus;

    private Boolean supportRefund;

    private Boolean supportBooking;

    private String merchantName;

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getBrandSlogan() {
        return brandSlogan;
    }

    public void setBrandSlogan(String brandSlogan) {
        this.brandSlogan = brandSlogan;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getServiceTags() {
        return serviceTags;
    }

    public void setServiceTags(List<String> serviceTags) {
        this.serviceTags = serviceTags;
    }

    public List<String> getBannerTitles() {
        return bannerTitles;
    }

    public void setBannerTitles(List<String> bannerTitles) {
        this.bannerTitles = bannerTitles;
    }

    public Boolean getBusinessStatus() {
        return businessStatus;
    }

    public void setBusinessStatus(Boolean businessStatus) {
        this.businessStatus = businessStatus;
    }

    public Boolean getSupportRefund() {
        return supportRefund;
    }

    public void setSupportRefund(Boolean supportRefund) {
        this.supportRefund = supportRefund;
    }

    public Boolean getSupportBooking() {
        return supportBooking;
    }

    public void setSupportBooking(Boolean supportBooking) {
        this.supportBooking = supportBooking;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
}
