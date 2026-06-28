package com.ruoyi.wxmini.task;

import com.ruoyi.mall.common.service.IWxPayOrderService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.service.IMallOrderService;
import com.ruoyi.mall.pay.domain.PaymentRecord;
import com.ruoyi.mall.pay.service.IPaymentRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
public class PendingOrderExpireTask {

    private static final Logger log = LoggerFactory.getLogger(PendingOrderExpireTask.class);

    @Value("${wxmini.order-expire.enabled:true}")
    private boolean enabled;

    @Value("${wxmini.order-expire.minutes:30}")
    private int expireMinutes;

    @Value("${wxmini.order-expire.batch-size:100}")
    private int batchSize;

    @Resource
    private IMallOrderService mallOrderService;
    @Resource
    private IPaymentRecordService paymentRecordService;
    @Resource
    private IWxPayOrderService wxPayOrderService;

    @Scheduled(fixedDelayString = "${wxmini.order-expire.fixed-delay-ms:60000}",
            initialDelayString = "${wxmini.order-expire.initial-delay-ms:60000}")
    public void cancelExpiredPendingOrders() {
        if (!enabled) {
            return;
        }
        Date expireBefore = new Date(System.currentTimeMillis() - Math.max(1, expireMinutes) * 60_000L);
        List<MallOrder> orders = mallOrderService.selectPendingOrdersCreatedBefore(expireBefore, Math.max(1, batchSize));
        if (orders == null || orders.isEmpty()) {
            return;
        }

        int cancelled = 0;
        for (MallOrder order : orders) {
            if (order == null || order.getOrderNo() == null) {
                continue;
            }
            try {
                PaymentRecord paymentRecord = paymentRecordService.selectByOrderNo(order.getOrderNo());
                boolean success = paymentRecord != null
                        ? Boolean.TRUE.equals(wxPayOrderService.queryPayResultAndUpdOrderStatus(order.getOrderNo()))
                        : false;
                if (!success) {
                    boolean closed = Boolean.TRUE.equals(wxPayOrderService.closeOrder(order.getOrderNo()));
                    if (closed) {
                        cancelled++;
                    }
                }
            } catch (Exception e) {
                log.warn("超时待支付订单自动取消失败: orderNo={}, reason={}", order.getOrderNo(), e.getMessage());
                try {
                    if (Boolean.TRUE.equals(wxPayOrderService.closeOrder(order.getOrderNo()))) {
                        cancelled++;
                    }
                } catch (Exception closeException) {
                    log.warn("超时待支付订单兜底关闭失败: orderNo={}, reason={}",
                            order.getOrderNo(), closeException.getMessage());
                }
            }
        }
        if (cancelled > 0) {
            log.info("已自动取消{}笔超过{}分钟未支付订单", cancelled, expireMinutes);
        }
    }
}
