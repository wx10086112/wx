package com.ruoyi.mall.order.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.mall.common.event.RefundApprovedEvent;
import com.ruoyi.mall.order.constant.MallOrderStatus;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.OrderItem;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.mapper.OrderItemMapper;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import com.ruoyi.mall.order.service.IMallOrderService;
import com.ruoyi.mall.product.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private IProductService productService;

    @Override
    public MallOrder selectMallOrderById(Long id) {
        return mallOrderMapper.selectMallOrderById(id);
    }

    @Override
    public MallOrder selectOrderByWriteOffCode(String code) {
        return mallOrderMapper.selectOrderByWriteOffCode(code);
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
        }

        if (refundRecord.getParams() == null) {
            refundRecord.setParams(new java.util.HashMap<>());
        }
        refundRecord.getParams().put("oldStatus", 1);

        int result = refundRecordMapper.updateRefundRecord(refundRecord);
        if (result == 0) {
            throw new RuntimeException("退款记录状态已变更，请刷新后重试");
        }

        // 退款审批通过（status=2）只触发微信退款，订单/结算等最终状态等待微信退款成功回调。
        if (status != null && status == 2 && refundRecord.getOrderNo() != null) {
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
    public boolean markOrderPaid(String orderNo, Date payTime) {
        return mallOrderMapper.markOrderPaid(orderNo, payTime) > 0;
    }

    @Override
    public boolean markOrderRefunded(String orderNo, Date refundTime) {
        return mallOrderMapper.markOrderRefunded(orderNo, refundTime) > 0;
    }

    @Override
    public int insertMallOrder(MallOrder mallOrder) {
        return mallOrderMapper.insertMallOrder(mallOrder);
    }

    @Override
    public int insertOrderItem(OrderItem orderItem) {
        return orderItemMapper.insertOrderItem(orderItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrderWithItems(MallOrder mallOrder, List<OrderItem> orderItems) {
        if (mallOrder == null || orderItems == null || orderItems.isEmpty()) {
            throw new RuntimeException("订单信息不能为空");
        }

        for (OrderItem orderItem : orderItems) {
            if (!productService.deductStock(orderItem.getProductId(), orderItem.getQuantity())) {
                throw new RuntimeException("商品库存不足: " + orderItem.getProductName());
            }
        }

        mallOrderMapper.insertMallOrder(mallOrder);
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(mallOrder.getId());
            orderItemMapper.insertOrderItem(orderItem);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelPendingOrder(String orderNo) {
        MallOrder order = mallOrderMapper.selectMallOrderByOrderNo(orderNo);
        if (order == null || order.getStatus() == null || order.getStatus() != MallOrderStatus.PENDING) {
            return false;
        }

        int affectedRows = mallOrderMapper.cancelPendingOrder(orderNo, new Date());
        if (affectedRows == 0) {
            return false;
        }

        List<OrderItem> orderItems = orderItemMapper.selectOrderItemByOrderNo(orderNo);
        if (orderItems != null) {
            for (OrderItem orderItem : orderItems) {
                productService.restoreStock(orderItem.getProductId(), orderItem.getQuantity());
            }
        }
        return true;
    }
}
