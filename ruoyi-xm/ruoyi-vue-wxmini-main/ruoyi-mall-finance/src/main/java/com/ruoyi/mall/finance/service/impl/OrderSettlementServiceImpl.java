package com.ruoyi.mall.finance.service.impl;

import com.ruoyi.mall.finance.domain.OrderProfitLedger;
import com.ruoyi.mall.finance.service.IDistributorSettlementRecordService;
import com.ruoyi.mall.finance.service.IMerchantSettlementRecordService;
import com.ruoyi.mall.finance.service.IOrderProfitLedgerService;
import com.ruoyi.mall.finance.service.IPlatformIncomeService;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.event.OrderCompletedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * Creates local financial records for a completed order in one transaction.
 */
@Service
public class OrderSettlementServiceImpl {

    @Resource
    private IOrderProfitLedgerService profitLedgerService;
    @Resource
    private IPlatformIncomeService platformIncomeService;
    @Resource
    private IMerchantSettlementRecordService merchantSettlementService;
    @Resource
    private IDistributorSettlementRecordService distributorSettlementService;
    @Resource
    private IMerchantService merchantService;

    @Transactional(rollbackFor = Exception.class)
    public OrderProfitLedger createSettlementRecords(OrderCompletedEvent event) {
        if (event == null || event.getOrderNo() == null || event.getMerchantId() == null) {
            throw new IllegalArgumentException("completed order settlement event is incomplete");
        }

        Long distributorId = resolveDistributorId(event.getMerchantId(), event.getDistributorId());
        profitLedgerService.createLedger(event.getOrderNo(), event.getMerchantId(), distributorId, event.getPayAmount());

        OrderProfitLedger ledger = profitLedgerService.selectByOrderNo(event.getOrderNo());
        if (ledger == null) {
            throw new IllegalStateException("order profit ledger was not created: " + event.getOrderNo());
        }

        if (positive(ledger.getPlatformAmount())) {
            platformIncomeService.createIncome(event.getOrderNo(), event.getMerchantId(),
                    ledger.getPayAmount(), ledger.getPlatformAmount());
        }

        merchantSettlementService.createSettlementForOrder(event.getOrderNo(), event.getMerchantId(),
                event.getStoreId(), ledger.getPayAmount(), ledger.getMerchantAmount(),
                ledger.getPlatformAmount(), event.getTitle());

        if (distributorId != null && positive(ledger.getDistributorAmount())) {
            distributorSettlementService.createSettlementForOrder(event.getOrderNo(), event.getMerchantId(),
                    distributorId, ledger.getDistributorAmount(), ledger.getDistributorRate());
        }
        return ledger;
    }

    private Long resolveDistributorId(Long merchantId, Long distributorId) {
        if (distributorId != null) {
            return distributorId;
        }
        Merchant merchant = merchantService.selectMerchantById(merchantId);
        return merchant != null ? merchant.getDistributorId() : null;
    }

    private boolean positive(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
