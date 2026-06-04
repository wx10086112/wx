package com.ruoyi.mall.order.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.mall.common.event.RefundApprovedEvent;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.OrderItem;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.mapper.OrderItemMapper;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import com.ruoyi.mall.order.service.IMallOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class MallOrderServiceImpl implements IMallOrderService {

    private static final int ORDER_STATUS_REFUNDED = 3;

    @Autowired
    private MallOrderMapper mallOrderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private RefundRecordMapper refundRecordMapper;
    @Autowired
    private ApplicationContext applicationContext;

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
    @Transactional(rollbackFor = Exception.class)
    public int handleRefund(Long id, Integer status, String operator, String rejectReason) {
        RefundRecord refundRecord = refundRecordMapper.selectRefundRecordById(id);
        if (refundRecord == null) {
            throw new RuntimeException("退款记录不存在");
        }

        if (refundRecord.getStatus() != null && refundRecord.getStatus() != 1) {
            throw new RuntimeException("退款记录非待处理状态，无法操作");
        }

        refundRecord.setStatus(status);
        refundRecord.setOperator(operator);
        refundRecord.setAuditTime(new Date());
        
        if (status != null && status == 3) {
            if (rejectReason != null && !rejectReason.isEmpty()) {
                refundRecord.setRejectReason(rejectReason);
            }
        } else if (status != null && status == 2) {
            refundRecord.setRefundTime(new Date());
        }

        if (refundRecord.getParams() == null) {
            refundRecord.setParams(new java.util.HashMap<>());
        }
        refundRecord.getParams().put("oldStatus", 1);

        int result = refundRecordMapper.updateRefundRecord(refundRecord);
        if (result == 0) {
            throw new RuntimeException("退款记录状态已变更，请刷新后重试");
        }

        // 退款审批通过（status=2）→ 发布事件触发结算逆向
        if (status != null && status == 2 && refundRecord.getOrderNo() != null) {
            // 更新订单状态为已退款
            MallOrder order = mallOrderMapper.selectMallOrderByOrderNo(refundRecord.getOrderNo());
            if (order != null && order.getStatus() != null && order.getStatus() < ORDER_STATUS_REFUNDED) {
                order.setStatus(ORDER_STATUS_REFUNDED);
                order.setRefundTime(new Date());
                mallOrderMapper.updateMallOrder(order);
            }

            // 发布退款通过事件（由财务模块监听处理结算逆向）
            applicationContext.publishEvent(new RefundApprovedEvent(this, refundRecord.getOrderNo(), id, operator));
        }

        return result;
    }

    @Override
    public boolean isMerchantAccessibleByDistributor(Long merchantId, Long distributorId) {
        if (merchantId == null || distributorId == null) {
            return false;
        }
        return mallOrderMapper.countMerchantByDistributor(merchantId, distributorId) > 0;
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
