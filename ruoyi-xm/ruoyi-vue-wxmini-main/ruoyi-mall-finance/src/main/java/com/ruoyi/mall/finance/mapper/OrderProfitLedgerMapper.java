package com.ruoyi.mall.finance.mapper;

import com.ruoyi.mall.finance.domain.OrderProfitLedger;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface OrderProfitLedgerMapper {

    OrderProfitLedger selectById(Long id);

    OrderProfitLedger selectByOrderNo(String orderNo);

    List<OrderProfitLedger> selectList(OrderProfitLedger query);

    int insert(OrderProfitLedger record);

    int updateById(OrderProfitLedger record);

    int claimProfitSharingAttempt(@Param("id") Long id,
                                  @Param("maxAttempts") int maxAttempts,
                                  @Param("nextRetryTime") Date nextRetryTime);

    int updateProfitSharingRequest(@Param("id") Long id, @Param("outOrderNo") String outOrderNo);

    int updateProfitSharingState(@Param("id") Long id,
                                 @Param("status") String status,
                                 @Param("remark") String remark,
                                 @Param("orderId") String orderId,
                                 @Param("nextRetryTime") Date nextRetryTime,
                                 @Param("clearNextRetry") boolean clearNextRetry);

    List<OrderProfitLedger> selectProfitSharingRetryCandidates(@Param("limit") int limit,
                                                                @Param("maxAttempts") int maxAttempts);

    List<OrderProfitLedger> selectProcessingProfitSharing(@Param("limit") int limit);

    BigDecimal sumMerchantAmountByMerchantId(@Param("merchantId") Long merchantId);

    BigDecimal sumPlatformAmount();

    BigDecimal sumDistributorAmountByDistributorId(@Param("distributorId") Long distributorId);

    Integer countByMerchantId(@Param("merchantId") Long merchantId);

    List<OrderProfitLedger> selectByDistributorId(@Param("distributorId") Long distributorId);
}
