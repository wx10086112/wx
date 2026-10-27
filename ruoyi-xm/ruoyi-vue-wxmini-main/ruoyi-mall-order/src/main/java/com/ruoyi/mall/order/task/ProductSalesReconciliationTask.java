package com.ruoyi.mall.order.task;

import com.ruoyi.mall.product.service.IProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 商品销量对账任务。
 *
 * <p>支付回调、退款回调都会实时增减 {@code product.sales}，一旦回调丢失或重复，
 * 销量会与订单明细长期不一致（小程序热销排序、后台统计都会受影响）。
 * 这里按有效订单（已支付/已完成/已核销）定时全量重算，只更新有差异的商品。</p>
 */
@Component
public class ProductSalesReconciliationTask {

    private static final Logger log = LoggerFactory.getLogger(ProductSalesReconciliationTask.class);

    @Resource
    private IProductService productService;

    /**
     * 默认启动 60 秒后跑第一次，之后每 6 小时跑一次，可用配置覆盖。
     */
    @Scheduled(initialDelayString = "${mall.product-sales.reconcile-initial-delay-ms:60000}",
            fixedDelayString = "${mall.product-sales.reconcile-fixed-delay-ms:21600000}")
    public void reconcile() {
        try {
            int updated = productService.reconcileSalesFromOrders();
            if (updated > 0) {
                log.info("product sales reconciled from effective orders: updated={}", updated);
            }
        } catch (Exception e) {
            log.error("product sales reconciliation failed: {}", e.getMessage(), e);
        }
    }
}
