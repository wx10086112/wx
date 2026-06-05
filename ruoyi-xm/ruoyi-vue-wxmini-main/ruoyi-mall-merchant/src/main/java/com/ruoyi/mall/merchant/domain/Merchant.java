package com.ruoyi.mall.merchant.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;

public class Merchant extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 状态常量 */
    public static final int STATUS_DISABLED = 0;    // 禁用
    public static final int STATUS_NORMAL = 1;      // 正常
    public static final int STATUS_PENDING = 2;     // 待审核（历史兼容）
    public static final int STATUS_STOPPED = 3;     // 停止合作

    private Long id;
    private Long distributorId;
    private String name;
    private String logo;
    private String contact;
    private String phone;
    private BigDecimal commissionRate;
    private Integer status;
    private BigDecimal balance;
    private BigDecimal totalIncome;
    private String address;
    private String avatar;
    private String description;
    private String businessHours;
    private Integer supportRefund;
    private Integer supportBooking;
    private Integer productCount;
    private Integer storeCount;
    private String cMiniAppId;
    @JsonIgnore
    private String cMiniAppSecret;
    private String mMiniAppId;
    @JsonIgnore
    private String mMiniAppSecret;
    private String wxPayMchId;
    @JsonIgnore
    private String wxPayApiKey;
    private String receiverOpenid;
    private String receiverType;

    // 腾讯地图认领字段
    private String mapClaimStatus;      // 认领状态: NOT_CLAIMED/CLAIMING/CLAIMED/FAILED
    private String mapPoiId;            // 腾讯地图POI ID
    private String mapClaimUrl;         // 认领或门店链接
    private java.util.Date mapClaimTime; // 认领完成时间
    private String mapClaimRemark;      // 认领备注

    // 微信特约商户字段
    private String wxApplymentId;           // 微信进件申请单号
    private String wxApplymentState;        // 微信进件状态: NOT_SUBMITTED/SUBMITTED/AUDITING/NEED_VERIFY/NEED_SIGN/FINISHED/REJECTED/FROZEN
    private String wxApplymentRejectReason; // 微信进件驳回原因
    private java.util.Date wxApplymentTime;         // 微信进件提交时间
    private java.util.Date wxApplymentFinishTime;   // 微信进件完成时间

    // 微信支付接入与三方分账配置
    private String wxPaymentAccessType;
    private String merchantWxMchId;
    private String merchantWxMchName;
    private Integer wxProfitSharingEnabled;
    private String platformReceiverMchId;
    private String distributorReceiverMchId;
    private BigDecimal merchantShareRate;
    private BigDecimal platformShareRate;
    private BigDecimal distributorShareRate;
    private String settlementCycle;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag = "0";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDistributorId() { return distributorId; }
    public void setDistributorId(Long distributorId) { this.distributorId = distributorId; }
    /** 分销商名称（LEFT JOIN） */
    private String distributorName;
    public String getDistributorName() { return distributorName; }
    public void setDistributorName(String distributorName) { this.distributorName = distributorName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
    public Integer getSupportRefund() { return supportRefund; }
    public void setSupportRefund(Integer supportRefund) { this.supportRefund = supportRefund; }
    public Integer getSupportBooking() { return supportBooking; }
    public void setSupportBooking(Integer supportBooking) { this.supportBooking = supportBooking; }
    public Integer getProductCount() { return productCount; }
    public void setProductCount(Integer productCount) { this.productCount = productCount; }
    public Integer getStoreCount() { return storeCount; }
    public void setStoreCount(Integer storeCount) { this.storeCount = storeCount; }
    public String getCMiniAppId() { return cMiniAppId; }
    public void setCMiniAppId(String cMiniAppId) { this.cMiniAppId = cMiniAppId; }
    public String getCMiniAppSecret() { return cMiniAppSecret; }
    public void setCMiniAppSecret(String cMiniAppSecret) { this.cMiniAppSecret = cMiniAppSecret; }
    public String getMMiniAppId() { return mMiniAppId; }
    public void setMMiniAppId(String mMiniAppId) { this.mMiniAppId = mMiniAppId; }
    public String getMMiniAppSecret() { return mMiniAppSecret; }
    public void setMMiniAppSecret(String mMiniAppSecret) { this.mMiniAppSecret = mMiniAppSecret; }
    public String getWxPayMchId() { return wxPayMchId; }
    public void setWxPayMchId(String wxPayMchId) { this.wxPayMchId = wxPayMchId; }
    public String getWxPayApiKey() { return wxPayApiKey; }
    public void setWxPayApiKey(String wxPayApiKey) { this.wxPayApiKey = wxPayApiKey; }
    public String getReceiverOpenid() { return receiverOpenid; }
    public void setReceiverOpenid(String receiverOpenid) { this.receiverOpenid = receiverOpenid; }
    public String getReceiverType() { return receiverType; }
    public void setReceiverType(String receiverType) { this.receiverType = receiverType; }

    // 腾讯地图认领字段 getter/setter
    public String getMapClaimStatus() { return mapClaimStatus; }
    public void setMapClaimStatus(String mapClaimStatus) { this.mapClaimStatus = mapClaimStatus; }
    public String getMapPoiId() { return mapPoiId; }
    public void setMapPoiId(String mapPoiId) { this.mapPoiId = mapPoiId; }
    public String getMapClaimUrl() { return mapClaimUrl; }
    public void setMapClaimUrl(String mapClaimUrl) { this.mapClaimUrl = mapClaimUrl; }
    public java.util.Date getMapClaimTime() { return mapClaimTime; }
    public void setMapClaimTime(java.util.Date mapClaimTime) { this.mapClaimTime = mapClaimTime; }
    public String getMapClaimRemark() { return mapClaimRemark; }
    public void setMapClaimRemark(String mapClaimRemark) { this.mapClaimRemark = mapClaimRemark; }

    // 微信特约商户字段 getter/setter
    public String getWxApplymentId() { return wxApplymentId; }
    public void setWxApplymentId(String wxApplymentId) { this.wxApplymentId = wxApplymentId; }
    public String getWxApplymentState() { return wxApplymentState; }
    public void setWxApplymentState(String wxApplymentState) { this.wxApplymentState = wxApplymentState; }
    public String getWxApplymentRejectReason() { return wxApplymentRejectReason; }
    public void setWxApplymentRejectReason(String wxApplymentRejectReason) { this.wxApplymentRejectReason = wxApplymentRejectReason; }
    public java.util.Date getWxApplymentTime() { return wxApplymentTime; }
    public void setWxApplymentTime(java.util.Date wxApplymentTime) { this.wxApplymentTime = wxApplymentTime; }
    public java.util.Date getWxApplymentFinishTime() { return wxApplymentFinishTime; }
    public void setWxApplymentFinishTime(java.util.Date wxApplymentFinishTime) { this.wxApplymentFinishTime = wxApplymentFinishTime; }
    public String getWxPaymentAccessType() { return wxPaymentAccessType; }
    public void setWxPaymentAccessType(String wxPaymentAccessType) { this.wxPaymentAccessType = wxPaymentAccessType; }
    public String getMerchantWxMchId() { return merchantWxMchId; }
    public void setMerchantWxMchId(String merchantWxMchId) { this.merchantWxMchId = merchantWxMchId; }
    public String getMerchantWxMchName() { return merchantWxMchName; }
    public void setMerchantWxMchName(String merchantWxMchName) { this.merchantWxMchName = merchantWxMchName; }
    public Integer getWxProfitSharingEnabled() { return wxProfitSharingEnabled; }
    public void setWxProfitSharingEnabled(Integer wxProfitSharingEnabled) { this.wxProfitSharingEnabled = wxProfitSharingEnabled; }
    public String getPlatformReceiverMchId() { return platformReceiverMchId; }
    public void setPlatformReceiverMchId(String platformReceiverMchId) { this.platformReceiverMchId = platformReceiverMchId; }
    public String getDistributorReceiverMchId() { return distributorReceiverMchId; }
    public void setDistributorReceiverMchId(String distributorReceiverMchId) { this.distributorReceiverMchId = distributorReceiverMchId; }
    public BigDecimal getMerchantShareRate() { return merchantShareRate; }
    public void setMerchantShareRate(BigDecimal merchantShareRate) { this.merchantShareRate = merchantShareRate; }
    public BigDecimal getPlatformShareRate() { return platformShareRate; }
    public void setPlatformShareRate(BigDecimal platformShareRate) { this.platformShareRate = platformShareRate; }
    public BigDecimal getDistributorShareRate() { return distributorShareRate; }
    public void setDistributorShareRate(BigDecimal distributorShareRate) { this.distributorShareRate = distributorShareRate; }
    public String getSettlementCycle() { return settlementCycle; }
    public void setSettlementCycle(String settlementCycle) { this.settlementCycle = settlementCycle; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    /**
     * 检查商户是否可以正式运营（上架团购商品）
     * 需要同时满足：status=1 + 地图认领 + 小程序AppID + 进件完成 + 子商户号 + 签约 + 结算账户 + 分账授权
     */
    public boolean canOperate() {
        return getOperateBlockReason() == null;
    }

    /**
     * 获取运营准入校验失败原因
     */
    public String getOperateBlockReason() {
        if (this.status == null || this.status != STATUS_NORMAL) {
            return "商户状态异常";
        }
        if (this.cMiniAppId == null || this.cMiniAppId.isEmpty()) {
            return "C端小程序AppID未配置";
        }
        if (!hasText(getEffectiveMerchantWxMchId())) {
            return "商家微信支付商户号未配置";
        }
        if (this.wxProfitSharingEnabled == null || this.wxProfitSharingEnabled != 1) {
            return "微信分账未开启";
        }
        if (!shareRatesValid()) {
            return "商家/平台/分销商三方分账比例合计必须为100%";
        }
        if (positive(this.platformShareRate) && !hasText(this.platformReceiverMchId)) {
            return "平台分账接收方未配置";
        }
        if (positive(this.distributorShareRate) && !hasText(this.distributorReceiverMchId)) {
            return "分销商分账接收方未配置";
        }
        return null;
    }

    public String getEffectiveMerchantWxMchId() {
        if (hasText(this.merchantWxMchId)) {
            return this.merchantWxMchId;
        }
        return null;
    }

    private boolean shareRatesValid() {
        if (this.merchantShareRate == null || this.platformShareRate == null || this.distributorShareRate == null) {
            return false;
        }
        BigDecimal sum = this.merchantShareRate.add(this.platformShareRate).add(this.distributorShareRate);
        return sum.compareTo(new BigDecimal("100")) == 0;
    }

    private boolean positive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
