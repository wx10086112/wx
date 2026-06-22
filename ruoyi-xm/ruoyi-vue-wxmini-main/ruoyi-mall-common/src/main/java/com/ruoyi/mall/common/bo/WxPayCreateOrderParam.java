package com.ruoyi.mall.common.bo;

public class WxPayCreateOrderParam {

    private String orderNo;
    private String orderDesc;
    private Integer amount;
    private String spAppId;
    private String spMchId;
    private String subAppId;
    private String subMchId;
    private String appId;
    private String openId;
    private String timeExpire;
    private Boolean profitSharing;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderDesc() {
        return orderDesc;
    }

    public void setOrderDesc(String orderDesc) {
        this.orderDesc = orderDesc;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getSpAppId() {
        return spAppId;
    }

    public void setSpAppId(String spAppId) {
        this.spAppId = spAppId;
    }

    public String getSpMchId() {
        return spMchId;
    }

    public void setSpMchId(String spMchId) {
        this.spMchId = spMchId;
    }

    public String getSubAppId() {
        return subAppId;
    }

    public void setSubAppId(String subAppId) {
        this.subAppId = subAppId;
    }

    public String getSubMchId() {
        return subMchId;
    }

    public void setSubMchId(String subMchId) {
        this.subMchId = subMchId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getSubOpenId() {
        return openId;
    }

    public void setSubOpenId(String subOpenId) {
        this.openId = subOpenId;
    }

    public String getTimeExpire() {
        return timeExpire;
    }

    public void setTimeExpire(String timeExpire) {
        this.timeExpire = timeExpire;
    }

    public Boolean getProfitSharing() {
        return profitSharing;
    }

    public void setProfitSharing(Boolean profitSharing) {
        this.profitSharing = profitSharing;
    }
}
