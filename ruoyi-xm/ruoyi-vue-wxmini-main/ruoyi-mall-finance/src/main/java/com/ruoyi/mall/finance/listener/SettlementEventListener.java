package com.ruoyi.mall.finance.listener;

import com.ruoyi.mall.finance.domain.OrderProfitLedger;
import com.ruoyi.mall.finance.service.IDistributorSettlementRecordService;
import com.ruoyi.mall.finance.service.IMerchantSettlementRecordService;
import com.ruoyi.mall.finance.service.IOrderProfitLedgerService;
import com.ruoyi.mall.finance.service.IPlatformIncomeService;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.event.OrderCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Component
public class SettlementEventListener {

    private static final Logger log = LoggerFactory.getLogger(SettlementEventListener.class);

    @Resource
    private IMerchantSettlementRecordService settlementService;
    @Resource
    private IOrderProfitLedgerService profitLedgerService;
    @Resource
    private IDistributorSettlementRecordService distributorSettlementService;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private IPlatformIncomeService platformIncomeService;

    @Async
    @EventListener
    public void onOrderCompleted(OrderCompletedEvent event) {
        String orderNo = event.getOrderNo();
        Long merchantId = event.getMerchantId();
        Long storeId = event.getStoreId();
        BigDecimal payAmount = event.getPayAmount();
        String title = event.getTitle();

        try {
            Long distributorId = event.getDistributorId();
            if (distributorId == null) {
                try {
                    Merchant merchant = merchantService.selectMerchantById(merchantId);
                    if (merchant != null) {
                        distributorId = merchant.getDistributorId();
                    }
                } catch (Exception e) {
                    log.warn("查询商家 {} 分销商失败: {}", merchantId, e.getMessage());
                }
            }

            profitLedgerService.createLedger(orderNo, merchantId, distributorId, payAmount);

            OrderProfitLedger ledger = profitLedgerService.selectByOrderNo(orderNo);
            if (ledger != null && ledger.getPlatformAmount() != null
                    && ledger.getPlatformAmount().compareTo(BigDecimal.ZERO) > 0) {
                platformIncomeService.createIncome(orderNo, merchantId, payAmount, ledger.getPlatformAmount());
            }

            if (ledger != null) {
                settlementService.createSettlementForOrder(orderNo, merchantId, storeId, ledger.getPayAmount(),
                        ledger.getMerchantAmount(), ledger.getPlatformAmount(), title);
            } else {
                BigDecimal merchantAmount = payAmount != null ? payAmount : BigDecimal.ZERO;
                settlementService.createSettlementForOrder(orderNo, merchantId, storeId, payAmount,
                        merchantAmount, BigDecimal.ZERO, title);
            }

            if (distributorId != null && ledger != null
                    && ledger.getDistributorAmount().compareTo(BigDecimal.ZERO) > 0) {
                distributorSettlementService.createSettlementForOrder(orderNo, merchantId, distributorId,
                        ledger.getDistributorAmount(), ledger.getDistributorRate());
            }

            log.info("订单 {} 三方结算记录生成完成: merchant={}, distributor={}", orderNo, merchantId, distributorId);
        } catch (Exception e) {
            log.error("订单 {} 生成结算记录失败: {}", orderNo, e.getMessage(), e);
        }
    }
}
