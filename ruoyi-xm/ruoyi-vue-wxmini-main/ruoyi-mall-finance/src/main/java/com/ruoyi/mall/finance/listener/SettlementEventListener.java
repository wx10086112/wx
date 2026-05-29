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
import java.math.RoundingMode;

@Component
public class SettlementEventListener {

    private static final Logger log = LoggerFactory.getLogger(SettlementEventListener.class);

    private static final BigDecimal DISTRIBUTOR_RATE = new BigDecimal("5");

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
            // 查商家获取分销商ID
            Long distributorId = event.getDistributorId();
            if (distributorId == null) {
                try {
                    Merchant merchant = merchantService.selectMerchantById(merchantId);
                    if (merchant != null) {
                        distributorId = merchant.getDistributorId();
                    }
                } catch (Exception e) {
                    log.warn("查询商家 {} 分销商ID失败: {}", merchantId, e.getMessage());
                }
            }

            // 1. 生成三方分账流水
            profitLedgerService.createLedger(orderNo, merchantId, distributorId, payAmount);

            // 2. 记录平台收入
            OrderProfitLedger ledger = profitLedgerService.selectByOrderNo(orderNo);
            if (ledger != null && ledger.getPlatformAmount() != null && ledger.getPlatformAmount().compareTo(BigDecimal.ZERO) > 0) {
                platformIncomeService.createIncome(orderNo, merchantId, payAmount, ledger.getPlatformAmount());
            }

            // 3. 生成商家结算记录（从分账流水取商家金额）
            if (ledger != null) {
                settlementService.createSettlementForOrder(orderNo, merchantId, storeId, ledger.getMerchantAmount(), title);
            } else {
                settlementService.createSettlementForOrder(orderNo, merchantId, storeId, payAmount, title);
            }

            // 4. 生成分销商结算记录（如果有分销商且佣金>0）
            if (distributorId != null && ledger != null && ledger.getDistributorAmount().compareTo(BigDecimal.ZERO) > 0) {
                distributorSettlementService.createSettlementForOrder(orderNo, merchantId, distributorId, ledger.getDistributorAmount(), DISTRIBUTOR_RATE);
            }

            log.info("订单 {} 三方结算记录生成完成: merchant={}, distributor={}", orderNo, merchantId, distributorId);
        } catch (Exception e) {
            log.error("订单 {} 生成结算记录失败: {}", orderNo, e.getMessage(), e);
        }
    }
}
