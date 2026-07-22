package com.ruoyi.wxmini.dto.wx;

import java.util.List;

public class WxGrouponItemDto {

    private Long id;
    private Long goodsId;
    private Long productId;
    private String title;
    private String subtitle;
    private Long merchantId;
    private String merchantName;
    private String image;
    private Long originalPrice;
    private Long price;
    private Integer sales;
    private Integer totalSales;
    private Integer stock;
    private Integer validDays;
    private String validPeriod;
    private String verifyNotice;
    private Long categoryId;
    private String categoryName;
    private List<String> tags;
    private String description;
    private List<String> contentDetail;
    private Boolean bookingRequired;
    private String bookingRule;
    private String refundRule;
    private String limitRule;
    private String status;
    private Integer sort;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGoodsId() { return goodsId; }
    public void setGoodsId(Long goodsId) { this.goodsId = goodsId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Long getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Long originalPrice) { this.originalPrice = originalPrice; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }
    public Integer getTotalSales() { return totalSales; }
    public void setTotalSales(Integer totalSales) { this.totalSales = totalSales; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }
    public String getValidPeriod() { return validPeriod; }
    public void setValidPeriod(String validPeriod) { this.validPeriod = validPeriod; }
    public String getVerifyNotice() { return verifyNotice; }
    public void setVerifyNotice(String verifyNotice) { this.verifyNotice = verifyNotice; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getContentDetail() { return contentDetail; }
    public void setContentDetail(List<String> contentDetail) { this.contentDetail = contentDetail; }
    public Boolean getBookingRequired() { return bookingRequired; }
    public void setBookingRequired(Boolean bookingRequired) { this.bookingRequired = bookingRequired; }
    public String getBookingRule() { return bookingRule; }
    public void setBookingRule(String bookingRule) { this.bookingRule = bookingRule; }
    public String getRefundRule() { return refundRule; }
    public void setRefundRule(String refundRule) { this.refundRule = refundRule; }
    public String getLimitRule() { return limitRule; }
    public void setLimitRule(String limitRule) { this.limitRule = limitRule; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
