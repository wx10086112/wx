package com.ruoyi.wxmini.service.impl;

import com.ruoyi.wxmini.domain.MallOrder;
import com.ruoyi.wxmini.domain.OrderItem;
import com.ruoyi.wxmini.domain.RefundRecord;
import com.ruoyi.wxmini.mapper.MallOrderMapper;
import com.ruoyi.wxmini.mapper.OrderItemMapper;
import com.ruoyi.wxmini.mapper.RefundRecordMapper;
import com.ruoyi.wxmini.service.IMallOrderService;
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
    public List<MallOrder> selectMallOrderList(MallOrder query) {
        return mallOrderMapper.selectMallOrderList(query);
    }

    @Override
    public int updateMallOrder(MallOrder mallOrder) {
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
    public List<RefundRecord> selectRefundList(RefundRecord query) {
        return refundRecordMapper.selectRefundRecordList(query);
    }

    @Override
    public RefundRecord selectRefundById(Long id) {
        return refundRecordMapper.selectRefundRecordById(id);
    }

    @Override
    public int handleRefund(Long id, Integer status, String operator, String rejectReason) {
        RefundRecord record = refundRecordMapper.selectRefundRecordById(id);
        if (record == null) {
            return 0;
        }
        record.setStatus(status);
        record.setOperator(operator);
        record.setAuditTime(new Date());
        if (status == 4) {
            record.setRejectReason(rejectReason);
        } else if (status == 3) {
            record.setRefundTime(new Date());
        }
        return refundRecordMapper.updateRefundRecord(record);
    }
}
