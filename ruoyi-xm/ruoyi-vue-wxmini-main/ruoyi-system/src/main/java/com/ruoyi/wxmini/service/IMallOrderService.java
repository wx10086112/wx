package com.ruoyi.wxmini.service;

import com.ruoyi.wxmini.domain.MallOrder;
import com.ruoyi.wxmini.domain.OrderItem;
import com.ruoyi.wxmini.domain.RefundRecord;
import java.util.List;

public interface IMallOrderService {
    MallOrder selectMallOrderById(Long id);
    List<MallOrder> selectMallOrderList(MallOrder query);
    int updateMallOrder(MallOrder mallOrder);

    List<OrderItem> selectOrderItemListByOrderId(Long orderId);
    List<OrderItem> selectOrderItemListByOrderNo(String orderNo);

    List<RefundRecord> selectRefundList(RefundRecord query);
    RefundRecord selectRefundById(Long id);
    int handleRefund(Long id, Integer status, String operator, String rejectReason);
}
