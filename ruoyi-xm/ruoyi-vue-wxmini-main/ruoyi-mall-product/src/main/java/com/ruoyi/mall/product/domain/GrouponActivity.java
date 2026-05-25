package com.ruoyi.mall.product.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

public class GrouponActivity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long merchantId;
    private String name;
    private String coverImage;
    /** 活动海报图 */
    private String posterImage;
    /** 活动详情图 JSON 数组 */
    private String detailImages;
    private String description;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private Integer totalSold;
    private Integer limitPerUser;
    /** 排序 */
    private Integer sort;
    /** 创建来源：ADMIN 总后台 / MERCHANT 商家端 */
    private String sourceType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getPosterImage() { return posterImage; }
    public void setPosterImage(String posterImage) { this.posterImage = posterImage; }

    public String getDetailImages() { return detailImages; }
    public void setDetailImages(String detailImages) { this.detailImages = detailImages; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getTotalSold() { return totalSold; }
    public void setTotalSold(Integer totalSold) { this.totalSold = totalSold; }

    public Integer getLimitPerUser() { return limitPerUser; }
    public void setLimitPerUser(Integer limitPerUser) { this.limitPerUser = limitPerUser; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
}
