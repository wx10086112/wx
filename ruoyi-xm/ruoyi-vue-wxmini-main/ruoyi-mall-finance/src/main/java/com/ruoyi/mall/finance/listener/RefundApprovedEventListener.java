package com.ruoyi.mall.finance.listener;

import com.ruoyi.mall.common.event.RefundSucceededEvent;
import com.ruoyi.mall.finance.service.IDistributorSettlementRecordService;
import com.ruoyi.mall.finance.service.IMerchantSettlementRecordService;
import com.ruoyi.mall.finance.service.IOrderProfitLedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 微信确认退款成功事件监听器。
 * 处理商家结算和分销商结算的逆向操作。
 */
@Component
public class RefundApprovedEventListener {

    private static final Logger log = LoggerFactory.getLogger(RefundApprovedEventListener.class);

    @Resource
    private IMerchantSettlementRecordService merchantSettlementService;
    @Resource
    private IDistributorSettlementRecordService distributorSettlementService;
    @Resource
    private IOrderProfitLedgerService profitLedgerService;

    @EventListener
    @Async
    public void onRefundSucceeded(RefundSucceededEvent event) {
        String orderNo = event.getOrderNo();
        log.info("收到微信退款成功事件: orderNo={}, refundId={}, refundNo={}",
                orderNo, event.getRefundRecordId(), event.getRefundNo());

        try {
            // 1. 商家结算逆向
            merchantSettlementService.handleRefundReverse(orderNo, "订单退款-微信确认成功");
            log.info("商家结算逆向完成: orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("商家结算逆向失败: orderNo={}, error={}", orderNo, e.getMessage(), e);
        }

        try {
            // 2. 分销商结算逆向
            distributorSettlementService.handleRefundReverse(orderNo);
            log.info("分销商结算逆向完成: orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("分销商结算逆向失败: orderNo={}, error={}", orderNo, e.getMessage(), e);
        }

        try {
            // 3. 分账流水逆向（标记为 REFUND_REVERSED）
            profitLedgerService.handleRefundReverse(orderNo);
            log.info("分账流水逆向完成: orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("分账流水逆向失败: orderNo={}, error={}", orderNo, e.getMessage(), e);
        }
    }
}
