package com.ruoyi.mall.merchant.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;

public class Merchant extends BaseEntity {
    private static final long serialVersionUID = 1L;

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
    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag = "0";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDistributorId() { return distributorId; }
    public void setDistributorId(Long distributorId) { this.distributorId = distributorId; }
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
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
