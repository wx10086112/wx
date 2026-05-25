package com.ruoyi.mall.product.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class ProductImage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    private Long merchantId;
    /** 图片类型: main主图/detail详情图/sku */
    private String imageType;
    private String imageUrl;
    private Integer sortOrder;
    /** SKU值(如: 红色/蓝色, 仅sku类型时填写) */
    private String skuValue;
    /** 状态: 0删除 1正常 */
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getSkuValue() { return skuValue; }
    public void setSkuValue(String skuValue) { this.skuValue = skuValue; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
