package com.ruoyi.mall.product.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

public class Distributor extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String contact;
    private String phone;
    private String username;
    @JsonIgnore
    private String password;
    private String regionCode;
    private String regionName;
    private Integer status;
    private String receiverOpenid;
    private String receiverType;
    private String remark;
    private Date createTime;
    private Date updateTime;
    private String delFlag = "0";

    /** 关联商家数（LEFT JOIN 统计） */
    private Integer merchantCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getReceiverOpenid() { return receiverOpenid; }
    public void setReceiverOpenid(String receiverOpenid) { this.receiverOpenid = receiverOpenid; }

    public String getReceiverType() { return receiverType; }
    public void setReceiverType(String receiverType) { this.receiverType = receiverType; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public Integer getMerchantCount() { return merchantCount; }
    public void setMerchantCount(Integer merchantCount) { this.merchantCount = merchantCount; }
}
