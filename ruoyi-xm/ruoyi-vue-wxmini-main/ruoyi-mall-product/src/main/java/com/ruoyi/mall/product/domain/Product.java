package com.ruoyi.mall.product.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;

public class Product extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long merchantId;
    private Long categoryId;
    private Long grouponId;
    private String name;
    private String coverImage;
    private String images;
    /** 商品主图URL(冗余，快速展示用) */
    private String mainImage;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    private Integer status;
    /** 核销方式: 1在线核销 2到店自提 */
    private Integer verifyType;
    private Integer validDays;
    private String description;
    private String storeIds;
    private Integer sort;
    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag = "0";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getGrouponId() { return grouponId; }
    public void setGrouponId(Long grouponId) { this.grouponId = grouponId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getVerifyType() { return verifyType; }
    public void setVerifyType(Integer verifyType) { this.verifyType = verifyType; }

    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStoreIds() { return storeIds; }
    public void setStoreIds(String storeIds) { this.storeIds = storeIds; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
