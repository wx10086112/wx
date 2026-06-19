package com.ruoyi.wxmini.dto.merchant;

import java.util.ArrayList;
import java.util.List;

public class MerchantMiniOrderDto {

    private Long orderId;

    private String orderNo;

    private Long goodsId;

    private String title;

    private String customerName;

    private String customerPhone;

    private Integer quantity;

    private Long payAmount;

    private String status;

    private Long createTime;

    private Long payTime;

    private String writeOffCode;

    private Long verifyTime;

    private String verifyStaffName;

    private String refundReason;

    private Long refundTime;

    private String refundRejectReason;

    private Long refundRejectTime;

    private String cancelReason;

    private Long cancelTime;

    private String remark;

    private List<HistoryItem> history = new ArrayList<>();

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(Long payAmount) {
        this.payAmount = payAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getPayTime() {
        return payTime;
    }

    public void setPayTime(Long payTime) {
        this.payTime = payTime;
    }

    public String getWriteOffCode() {
        return writeOffCode;
    }

    public void setWriteOffCode(String writeOffCode) {
        this.writeOffCode = writeOffCode;
    }

    public Long getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(Long verifyTime) {
        this.verifyTime = verifyTime;
    }

    public String getVerifyStaffName() {
        return verifyStaffName;
    }

    public void setVerifyStaffName(String verifyStaffName) {
        this.verifyStaffName = verifyStaffName;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public Long getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(Long refundTime) {
        this.refundTime = refundTime;
    }

    public String getRefundRejectReason() {
        return refundRejectReason;
    }

    public void setRefundRejectReason(String refundRejectReason) {
        this.refundRejectReason = refundRejectReason;
    }

    public Long getRefundRejectTime() {
        return refundRejectTime;
    }

    public void setRefundRejectTime(Long refundRejectTime) {
        this.refundRejectTime = refundRejectTime;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Long getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(Long cancelTime) {
        this.cancelTime = cancelTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<HistoryItem> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryItem> history) {
        this.history = history == null ? new ArrayList<>() : history;
    }

    public static class HistoryItem {
        private Integer fromStatus;
        private Integer toStatus;
        private String status;
        private String action;
        private String source;
        private String operatorName;
        private String remark;
        private Long changeTime;

        public Integer getFromStatus() {
            return fromStatus;
        }

        public void setFromStatus(Integer fromStatus) {
            this.fromStatus = fromStatus;
        }

        public Integer getToStatus() {
            return toStatus;
        }

        public void setToStatus(Integer toStatus) {
            this.toStatus = toStatus;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getOperatorName() {
            return operatorName;
        }

        public void setOperatorName(String operatorName) {
            this.operatorName = operatorName;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public Long getChangeTime() {
            return changeTime;
        }

        public void setChangeTime(Long changeTime) {
            this.changeTime = changeTime;
        }
    }
}
