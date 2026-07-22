package com.ruoyi.mall.finance.service.impl;

import com.ruoyi.mall.finance.domain.OrderProfitLedger;
import com.ruoyi.mall.finance.service.IDistributorSettlementRecordService;
import com.ruoyi.mall.finance.service.IMerchantSettlementRecordService;
import com.ruoyi.mall.finance.service.IOrderProfitLedgerService;
import com.ruoyi.mall.finance.service.IPlatformIncomeService;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.event.OrderCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderSettlementServiceImplTest {

    @InjectMocks
    private OrderSettlementServiceImpl service;
    @Mock
    private IOrderProfitLedgerService profitLedgerService;
    @Mock
    private IPlatformIncomeService platformIncomeService;
    @Mock
    private IMerchantSettlementRecordService merchantSettlementService;
    @Mock
    private IDistributorSettlementRecordService distributorSettlementService;
    @Mock
    private IMerchantService merchantService;

    @Test
    void createsAllFinancialRecordsForDistributorOrder() {
        Merchant merchant = new Merchant();
        merchant.setDistributorId(20L);
        OrderProfitLedger ledger = ledger(new BigDecimal("100.00"), new BigDecimal("84.49"),
                new BigDecimal("9.94"), new BigDecimal("4.97"), new BigDecimal("5.00"));
        when(merchantService.selectMerchantById(10L)).thenReturn(merchant);
        when(profitLedgerService.selectByOrderNo("ORDER-100")).thenReturn(ledger);

        OrderProfitLedger actual = service.createSettlementRecords(event());

        assertSame(ledger, actual);
        verify(profitLedgerService).createLedger("ORDER-100", 10L, 20L, new BigDecimal("100.00"));
        verify(platformIncomeService).createIncome("ORDER-100", 10L, new BigDecimal("100.00"), new BigDecimal("9.94"));
        verify(merchantSettlementService).createSettlementForOrder("ORDER-100", 10L, 30L,
                new BigDecimal("100.00"), new BigDecimal("84.49"), new BigDecimal("9.94"), "Test package");
        verify(distributorSettlementService).createSettlementForOrder("ORDER-100", 10L, 20L,
                new BigDecimal("4.97"), new BigDecimal("5.00"));
    }

    @Test
    void propagatesFailureSoTheTransactionCanRollback() {
        when(profitLedgerService.selectByOrderNo("ORDER-100")).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class, () -> service.createSettlementRecords(event()));

        verify(platformIncomeService, never()).createIncome(any(), any(), any(), any());
        verify(merchantSettlementService, never()).createSettlementForOrder(any(), any(), any(), any(), any(), any(), any());
        verify(distributorSettlementService, never()).createSettlementForOrder(any(), any(), any(), any(), any());
    }

    private OrderCompletedEvent event() {
        return new OrderCompletedEvent(this, "ORDER-100", 10L, null, 30L,
                new BigDecimal("100.00"), "Test package");
    }

    private OrderProfitLedger ledger(BigDecimal payAmount, BigDecimal merchantAmount,
                                    BigDecimal platformAmount, BigDecimal distributorAmount,
                                    BigDecimal distributorRate) {
        OrderProfitLedger ledger = new OrderProfitLedger();
        ledger.setPayAmount(payAmount);
        ledger.setMerchantAmount(merchantAmount);
        ledger.setPlatformAmount(platformAmount);
        ledger.setDistributorAmount(distributorAmount);
        ledger.setDistributorRate(distributorRate);
        return ledger;
    }
}
