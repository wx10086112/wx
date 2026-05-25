package com.ruoyi.mall.order.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.OrderItem;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.mapper.OrderItemMapper;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import com.ruoyi.mall.order.service.IMallOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MallOrderServiceImpl implements IMallOrderService {

    @Autowired
    private MallOrderMapper mallOrderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private RefundRecordMapper refundRecordMapper;

    @Override
    public MallOrder selectMallOrderById(Long id) {
        return mallOrderMapper.selectMallOrderById(id);
    }

    @Override
    public List<MallOrder> selectMallOrderList(MallOrder mallOrder) {
        return mallOrderMapper.selectMallOrderList(mallOrder);
    }

    @Override
    public int updateMallOrder(MallOrder mallOrder) {
        mallOrder.setUpdateTime(DateUtils.getNowDate());
        return mallOrderMapper.updateMallOrder(mallOrder);
    }

    @Override
    public List<OrderItem> selectOrderItemListByOrderId(Long orderId) {
        return orderItemMapper.selectOrderItemByOrderId(orderId);
    }

    @Override
    public List<OrderItem> selectOrderItemListByOrderNo(String orderNo) {
        return orderItemMapper.selectOrderItemByOrderNo(orderNo);
    }

    @Override
    public List<RefundRecord> selectRefundList(RefundRecord refundRecord) {
        return refundRecordMapper.selectRefundRecordList(refundRecord);
    }

    @Override
    public RefundRecord selectRefundById(Long id) {
        return refundRecordMapper.selectRefundRecordById(id);
    }

    @Override
    public int handleRefund(Long id, Integer status, String operator, String rejectReason) {
        RefundRecord refundRecord = new RefundRecord();
        refundRecord.setId(id);
        refundRecord.setStatus(status);
        refundRecord.setOperator(operator);
        refundRecord.setAuditTime(new Date());
        if (rejectReason != null && !rejectReason.isEmpty()) {
            refundRecord.setRejectReason(rejectReason);
        } else {
            refundRecord.setRefundTime(new Date());
        }
        return refundRecordMapper.updateRefundRecord(refundRecord);
    }

    @Override
    public MallOrder selectMallOrderByOrderNo(String orderNo) {
        return mallOrderMapper.selectMallOrderByOrderNo(orderNo);
    }

    @Override
    public int insertMallOrder(MallOrder mallOrder) {
        return mallOrderMapper.insertMallOrder(mallOrder);
    }

    @Override
    public int insertOrderItem(OrderItem orderItem) {
        return orderItemMapper.insertOrderItem(orderItem);
    }
}
