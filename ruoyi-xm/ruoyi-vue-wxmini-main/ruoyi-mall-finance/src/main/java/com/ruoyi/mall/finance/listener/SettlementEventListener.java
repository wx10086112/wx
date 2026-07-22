package com.ruoyi.mall.finance.listener;

import com.ruoyi.mall.finance.domain.OrderProfitLedger;
import com.ruoyi.mall.finance.service.IWechatProfitSharingService;
import com.ruoyi.mall.finance.service.impl.OrderSettlementServiceImpl;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.OrderItem;
import com.ruoyi.mall.order.event.OrderCompletedEvent;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.mapper.OrderItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;
import java.util.List;

@Component
public class SettlementEventListener {

    private static final Logger log = LoggerFactory.getLogger(SettlementEventListener.class);

    @Resource
    private OrderSettlementServiceImpl orderSettlementService;
    @Resource
    private IWechatProfitSharingService wechatProfitSharingService;
    @Resource
    private MallOrderMapper mallOrderMapper;
    @Resource
    private OrderItemMapper orderItemMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCompleted(OrderCompletedEvent event) {
        processSettlement(event, false);
    }

    @Scheduled(initialDelayString = "${mall.settlement-retry-initial-delay-ms:180000}",
            fixedDelayString = "${mall.settlement-retry-fixed-delay-ms:300000}")
    public void retryIncompleteSettlements() {
        List<MallOrder> orders = mallOrderMapper.selectCompletedOrdersMissingSettlement(20);
        if (orders == null || orders.isEmpty()) {
            return;
        }
        log.warn("detected {} completed orders with incomplete financial records", orders.size());
        for (MallOrder order : orders) {
            if (order == null || order.getOrderNo() == null || order.getMerchantId() == null) {
                continue;
            }
            processSettlement(new OrderCompletedEvent(this, order.getOrderNo(), order.getMerchantId(),
                    order.getDistributorId(), order.getStoreId(), order.getPayAmount(), resolveTitle(order.getOrderNo())), true);
        }
    }

    private void processSettlement(OrderCompletedEvent event, boolean retry) {
        try {
            OrderProfitLedger ledger = orderSettlementService.createSettlementRecords(event);
            if (ledger != null) {
                wechatProfitSharingService.processOrderProfitSharing(event.getOrderNo());
            }
            log.info("order settlement records ready: orderNo={}, retry={}", event.getOrderNo(), retry);
        } catch (Exception e) {
            log.error("order settlement record creation failed: orderNo={}, retry={}, error={}",
                    event.getOrderNo(), retry, e.getMessage(), e);
        }
    }

    private String resolveTitle(String orderNo) {
        List<OrderItem> items = orderItemMapper.selectOrderItemByOrderNo(orderNo);
        if (items != null && !items.isEmpty() && items.get(0).getProductName() != null) {
            return items.get(0).getProductName();
        }
        return orderNo;
    }
}
