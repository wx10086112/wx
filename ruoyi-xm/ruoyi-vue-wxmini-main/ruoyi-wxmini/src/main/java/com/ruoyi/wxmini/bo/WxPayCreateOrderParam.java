package com.ruoyi.wxmini.bo;

/**
 * 微信小程序支付下单参数
 *
 * @author weijiayu
 * @date 2025/6/12 23:10
 */
public class WxPayCreateOrderParam {

    private String orderNo;
    private String orderDesc;
    private Integer amount;
    private String openId;
    // 业务上可以保存该参数，提供给后端定时任务使用，判断订单是否已经超时；也可以提供给前端用于支付倒计时展示
    private String timeExpire;

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

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getTimeExpire() {
        return timeExpire;
    }

    public void setTimeExpire(String timeExpire) {
        this.timeExpire = timeExpire;
    }
}
