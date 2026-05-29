package com.ruoyi.mall.finance.mapper;

import com.ruoyi.mall.finance.domain.OrderProfitLedger;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderProfitLedgerMapper {

    OrderProfitLedger selectById(Long id);

    OrderProfitLedger selectByOrderNo(String orderNo);

    List<OrderProfitLedger> selectList(OrderProfitLedger query);

    int insert(OrderProfitLedger record);

    int updateById(OrderProfitLedger record);

    BigDecimal sumMerchantAmountByMerchantId(@Param("merchantId") Long merchantId);

    BigDecimal sumPlatformAmount();

    BigDecimal sumDistributorAmountByDistributorId(@Param("distributorId") Long distributorId);

    Integer countByMerchantId(@Param("merchantId") Long merchantId);

    List<OrderProfitLedger> selectByDistributorId(@Param("distributorId") Long distributorId);
}
