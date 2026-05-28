package com.ruoyi.mall.finance.service;

import com.ruoyi.mall.finance.domain.OrderProfitLedger;

import java.math.BigDecimal;
import java.util.List;

public interface IOrderProfitLedgerService {

    OrderProfitLedger selectById(Long id);

    OrderProfitLedger selectByOrderNo(String orderNo);

    List<OrderProfitLedger> selectList(OrderProfitLedger query);

    /**
     * 为订单生成分账流水（幂等）
     */
    void createLedger(String orderNo, Long merchantId, Long distributorId, BigDecimal payAmount);

    /**
     * 退款逆向：标记分账流水为 REFUND_REVERSED
     */
    void handleRefundReverse(String orderNo);

    BigDecimal sumMerchantAmountByMerchantId(Long merchantId);

    BigDecimal sumPlatformAmount();

    BigDecimal sumDistributorAmountByDistributorId(Long distributorId);

    Integer countByMerchantId(Long merchantId);

    List<OrderProfitLedger> selectByDistributorId(Long distributorId);
}
