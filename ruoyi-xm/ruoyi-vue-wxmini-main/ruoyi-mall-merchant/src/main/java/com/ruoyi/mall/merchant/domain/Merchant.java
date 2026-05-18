package com.ruoyi.mall.merchant.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;

public class Merchant extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
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
    private String cMiniAppSecret;
    private String mMiniAppId;
    private String mMiniAppSecret;
    private String wxPayMchId;
    private String wxPayApiKey;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
}
