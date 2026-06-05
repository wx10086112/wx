package com.ruoyi.wxmini.dto.wx;

import com.ruoyi.mall.merchant.domain.MerchantStore;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class WxMerchantItemDto {

    private Long id;
    private Long merchantId;
    private Long storeId;
    private String name;
    private String shortName;
    private String avatar;
    private String coverImage;
    private Integer sales;
    private Integer productCount;
    private Integer storeCount;
    private String address;
    private String distance;
    private Long distanceValue;
    private Long categoryId;
    private String categoryName;
    private String businessHours;
    private String businessHoursText;
    private String phone;
    private String contact;
    private String description;
    private Integer status;
    private Long distributorId;
    private String distributorName;
    private String cMiniAppId;
    private String mMiniAppId;
    private String wxPayMchId;
    private String wxApplymentId;
    private String wxApplymentState;
    private Date wxApplymentTime;
    private Date wxApplymentFinishTime;
    private String wxApplymentRejectReason;
    private Integer wxProfitSharingEnabled;
    private String platformReceiverMchId;
    private String distributorReceiverMchId;
    private BigDecimal merchantShareRate;
    private BigDecimal platformShareRate;
    private BigDecimal distributorShareRate;
    private String settlementCycle;
    private String mapClaimStatus;
    private String mapPoiId;
    private String mapClaimUrl;
    private Date mapClaimTime;
    private String mapClaimRemark;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> tags;
    private List<String> serviceAbilityTags;
    private List<String> facilityTags;
    private List<String> albumList;
    private List<MerchantStore> storeList;
    private String notice;
    private Boolean isHot;
    private Boolean businessStatus;
    private Boolean supportRefund;
    private Boolean supportBooking;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }

    public Integer getStoreCount() {
        return storeCount;
    }

    public void setStoreCount(Integer storeCount) {
        this.storeCount = storeCount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public Long getDistanceValue() {
        return distanceValue;
    }

    public void setDistanceValue(Long distanceValue) {
        this.distanceValue = distanceValue;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public String getBusinessHoursText() {
        return businessHoursText;
    }

    public void setBusinessHoursText(String businessHoursText) {
        this.businessHoursText = businessHoursText;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(Long distributorId) {
        this.distributorId = distributorId;
    }

    public String getDistributorName() {
        return distributorName;
    }

    public void setDistributorName(String distributorName) {
        this.distributorName = distributorName;
    }

    public String getCMiniAppId() {
        return cMiniAppId;
    }

    public void setCMiniAppId(String cMiniAppId) {
        this.cMiniAppId = cMiniAppId;
    }

    public String getMMiniAppId() {
        return mMiniAppId;
    }

    public void setMMiniAppId(String mMiniAppId) {
        this.mMiniAppId = mMiniAppId;
    }

    public String getWxPayMchId() {
        return wxPayMchId;
    }

    public void setWxPayMchId(String wxPayMchId) {
        this.wxPayMchId = wxPayMchId;
    }

    public String getWxApplymentId() {
        return wxApplymentId;
    }

    public void setWxApplymentId(String wxApplymentId) {
        this.wxApplymentId = wxApplymentId;
    }

    public String getWxApplymentState() {
        return wxApplymentState;
    }

    public void setWxApplymentState(String wxApplymentState) {
        this.wxApplymentState = wxApplymentState;
    }

    public Date getWxApplymentTime() {
        return wxApplymentTime;
    }

    public void setWxApplymentTime(Date wxApplymentTime) {
        this.wxApplymentTime = wxApplymentTime;
    }

    public Date getWxApplymentFinishTime() {
        return wxApplymentFinishTime;
    }

    public void setWxApplymentFinishTime(Date wxApplymentFinishTime) {
        this.wxApplymentFinishTime = wxApplymentFinishTime;
    }

    public String getWxApplymentRejectReason() {
        return wxApplymentRejectReason;
    }

    public void setWxApplymentRejectReason(String wxApplymentRejectReason) {
        this.wxApplymentRejectReason = wxApplymentRejectReason;
    }

    public Integer getWxProfitSharingEnabled() {
        return wxProfitSharingEnabled;
    }

    public void setWxProfitSharingEnabled(Integer wxProfitSharingEnabled) {
        this.wxProfitSharingEnabled = wxProfitSharingEnabled;
    }

    public String getPlatformReceiverMchId() {
        return platformReceiverMchId;
    }

    public void setPlatformReceiverMchId(String platformReceiverMchId) {
        this.platformReceiverMchId = platformReceiverMchId;
    }

    public String getDistributorReceiverMchId() {
        return distributorReceiverMchId;
    }

    public void setDistributorReceiverMchId(String distributorReceiverMchId) {
        this.distributorReceiverMchId = distributorReceiverMchId;
    }

    public BigDecimal getMerchantShareRate() {
        return merchantShareRate;
    }

    public void setMerchantShareRate(BigDecimal merchantShareRate) {
        this.merchantShareRate = merchantShareRate;
    }

    public BigDecimal getPlatformShareRate() {
        return platformShareRate;
    }

    public void setPlatformShareRate(BigDecimal platformShareRate) {
        this.platformShareRate = platformShareRate;
    }

    public BigDecimal getDistributorShareRate() {
        return distributorShareRate;
    }

    public void setDistributorShareRate(BigDecimal distributorShareRate) {
        this.distributorShareRate = distributorShareRate;
    }

    public String getSettlementCycle() {
        return settlementCycle;
    }

    public void setSettlementCycle(String settlementCycle) {
        this.settlementCycle = settlementCycle;
    }

    public String getMapClaimStatus() {
        return mapClaimStatus;
    }

    public void setMapClaimStatus(String mapClaimStatus) {
        this.mapClaimStatus = mapClaimStatus;
    }

    public String getMapPoiId() {
        return mapPoiId;
    }

    public void setMapPoiId(String mapPoiId) {
        this.mapPoiId = mapPoiId;
    }

    public String getMapClaimUrl() {
        return mapClaimUrl;
    }

    public void setMapClaimUrl(String mapClaimUrl) {
        this.mapClaimUrl = mapClaimUrl;
    }

    public Date getMapClaimTime() {
        return mapClaimTime;
    }

    public void setMapClaimTime(Date mapClaimTime) {
        this.mapClaimTime = mapClaimTime;
    }

    public String getMapClaimRemark() {
        return mapClaimRemark;
    }

    public void setMapClaimRemark(String mapClaimRemark) {
        this.mapClaimRemark = mapClaimRemark;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getServiceAbilityTags() {
        return serviceAbilityTags;
    }

    public void setServiceAbilityTags(List<String> serviceAbilityTags) {
        this.serviceAbilityTags = serviceAbilityTags;
    }

    public List<String> getFacilityTags() {
        return facilityTags;
    }

    public void setFacilityTags(List<String> facilityTags) {
        this.facilityTags = facilityTags;
    }

    public List<String> getAlbumList() {
        return albumList;
    }

    public void setAlbumList(List<String> albumList) {
        this.albumList = albumList;
    }

    public List<MerchantStore> getStoreList() {
        return storeList;
    }

    public void setStoreList(List<MerchantStore> storeList) {
        this.storeList = storeList;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public Boolean getIsHot() {
        return isHot;
    }

    public void setIsHot(Boolean isHot) {
        this.isHot = isHot;
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
}
