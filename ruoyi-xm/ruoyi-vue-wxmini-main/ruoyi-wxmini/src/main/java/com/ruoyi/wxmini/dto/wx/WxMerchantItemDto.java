package com.ruoyi.wxmini.dto.wx;

import java.math.BigDecimal;
import java.util.List;

public class WxMerchantItemDto {

    private Long id;
    private Long merchantId;
    private String name;
    private String shortName;
    private String avatar;
    private String coverImage;
    private Integer sales;
    private String address;
    private String distance;
    private Long distanceValue;
    private Long categoryId;
    private String categoryName;
    private String businessHours;
    private String businessHoursText;
    private String phone;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> tags;
    private List<String> serviceAbilityTags;
    private List<String> facilityTags;
    private List<String> albumList;
    private String notice;
    private Boolean isHot;
    private Boolean businessStatus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }
    public Long getDistanceValue() { return distanceValue; }
    public void setDistanceValue(Long distanceValue) { this.distanceValue = distanceValue; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
    public String getBusinessHoursText() { return businessHoursText; }
    public void setBusinessHoursText(String businessHoursText) { this.businessHoursText = businessHoursText; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<String> getServiceAbilityTags() { return serviceAbilityTags; }
    public void setServiceAbilityTags(List<String> serviceAbilityTags) { this.serviceAbilityTags = serviceAbilityTags; }
    public List<String> getFacilityTags() { return facilityTags; }
    public void setFacilityTags(List<String> facilityTags) { this.facilityTags = facilityTags; }
    public List<String> getAlbumList() { return albumList; }
    public void setAlbumList(List<String> albumList) { this.albumList = albumList; }
    public String getNotice() { return notice; }
    public void setNotice(String notice) { this.notice = notice; }
    public Boolean getIsHot() { return isHot; }
    public void setIsHot(Boolean isHot) { this.isHot = isHot; }
    public Boolean getBusinessStatus() { return businessStatus; }
    public void setBusinessStatus(Boolean businessStatus) { this.businessStatus = businessStatus; }
}
