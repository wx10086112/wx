package com.ruoyi.mall.order.mapper;

import com.ruoyi.mall.order.domain.OrderItem;

import java.util.List;

public interface OrderItemMapper {

    OrderItem selectOrderItemById(Long id);

    List<OrderItem> selectOrderItemList(OrderItem orderItem);

    List<OrderItem> selectOrderItemByOrderId(Long orderId);

    List<OrderItem> selectOrderItemByOrderNo(String orderNo);

    int insertOrderItem(OrderItem orderItem);

    int updateOrderItem(OrderItem orderItem);

    int deleteOrderItemById(Long id);

    int deleteOrderItemByIds(Long[] ids);
}
