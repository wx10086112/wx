package com.ruoyi.mall.finance.service;

import com.ruoyi.mall.finance.domain.DistributorSettlementRecord;

import java.math.BigDecimal;
import java.util.List;

public interface IDistributorSettlementRecordService {

    DistributorSettlementRecord selectById(Long id);

    DistributorSettlementRecord selectBySettlementNo(String settlementNo);

    List<DistributorSettlementRecord> selectList(DistributorSettlementRecord query);

    int updateById(DistributorSettlementRecord record);

    /**
     * 为订单生成分销商结算记录（幂等）
     */
    void createSettlementForOrder(String orderNo, Long merchantId, Long distributorId, BigDecimal commissionAmount, BigDecimal rate);

    /**
     * 退款逆向
     */
    void handleRefundReverse(String orderNo);

    /**
     * 批量标记已到账
     */
    void batchMarkArrived(List<Long> ids);

    /**
     * 标记失败
     */
    void markFailed(Long id, String failReason);

    BigDecimal sumAmountByStatus(Long distributorId, String status);

    BigDecimal sumAmountByDistributorId(Long distributorId);
}
