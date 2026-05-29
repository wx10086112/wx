package com.ruoyi.mall.product.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;

public class GrouponActivityItem extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long merchantId;
    private Long grouponId;
    private String name;
    private String title;
    private String content;
    private String description;
    private String coverImage;
    private String detailImages;
    private BigDecimal originalPrice;
    private BigDecimal grouponPrice;
    private BigDecimal discountRate;
    private Integer stock;
    private Integer sales;
    private Integer limitPerUser;
    private Integer validDays;
    private String storeIds;
    /** 菜品组 JSON */
    private String dishGroups;
    /** 菜品总价，单位分 */
    private Long dishTotalPrice;
    /** 是否直接设置菜品总价 */
    private Integer directTotalPrice;
    /** 菜品数量统计 */
    private Integer dishCount;
    /** 实际可享用菜品数量 */
    private Integer availableDishCount;
    private Integer status;
    private Integer sort;
    private Date createTime;
    private Date updateTime;
    /** 删除标志（0存在 2删除） */
    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public Long getGrouponId() { return grouponId; }
    public void setGrouponId(Long grouponId) { this.grouponId = grouponId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getDetailImages() { return detailImages; }
    public void setDetailImages(String detailImages) { this.detailImages = detailImages; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public BigDecimal getGrouponPrice() { return grouponPrice; }
    public void setGrouponPrice(BigDecimal grouponPrice) { this.grouponPrice = grouponPrice; }

    public BigDecimal getDiscountRate() { return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }

    public Integer getLimitPerUser() { return limitPerUser; }
    public void setLimitPerUser(Integer limitPerUser) { this.limitPerUser = limitPerUser; }

    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }

    public String getStoreIds() { return storeIds; }
    public void setStoreIds(String storeIds) { this.storeIds = storeIds; }

    public String getDishGroups() { return dishGroups; }
    public void setDishGroups(String dishGroups) { this.dishGroups = dishGroups; }

    public Long getDishTotalPrice() { return dishTotalPrice; }
    public void setDishTotalPrice(Long dishTotalPrice) { this.dishTotalPrice = dishTotalPrice; }

    public Integer getDirectTotalPrice() { return directTotalPrice; }
    public void setDirectTotalPrice(Integer directTotalPrice) { this.directTotalPrice = directTotalPrice; }

    public Integer getDishCount() { return dishCount; }
    public void setDishCount(Integer dishCount) { this.dishCount = dishCount; }

    public Integer getAvailableDishCount() { return availableDishCount; }
    public void setAvailableDishCount(Integer availableDishCount) { this.availableDishCount = availableDishCount; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
