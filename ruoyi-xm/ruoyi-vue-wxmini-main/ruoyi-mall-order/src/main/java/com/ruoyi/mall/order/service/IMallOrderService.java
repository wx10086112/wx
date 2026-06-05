package com.ruoyi.mall.order.service;

import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.OrderItem;
import com.ruoyi.mall.order.domain.RefundRecord;

import java.util.List;

public interface IMallOrderService {

    MallOrder selectMallOrderById(Long id);

    MallOrder selectOrderByWriteOffCode(String code);

    List<MallOrder> selectMallOrderList(MallOrder mallOrder);

    int updateMallOrder(MallOrder mallOrder);

    List<OrderItem> selectOrderItemListByOrderId(Long orderId);

    List<OrderItem> selectOrderItemListByOrderNo(String orderNo);

    List<RefundRecord> selectRefundList(RefundRecord refundRecord);

    RefundRecord selectRefundById(Long id);

    int handleRefund(Long id, Integer status, String operator, String rejectReason);

    boolean isMerchantAccessibleByDistributor(Long merchantId, Long distributorId);

    MallOrder selectMallOrderByOrderNo(String orderNo);

    int insertMallOrder(MallOrder mallOrder);

    int insertOrderItem(OrderItem orderItem);
}
