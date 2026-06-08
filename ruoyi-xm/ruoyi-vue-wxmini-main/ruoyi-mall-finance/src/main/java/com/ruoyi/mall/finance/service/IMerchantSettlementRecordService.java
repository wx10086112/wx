package com.ruoyi.mall.finance.service;

import com.ruoyi.mall.finance.domain.MerchantSettlementRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IMerchantSettlementRecordService {

    MerchantSettlementRecord selectById(Long id);

    MerchantSettlementRecord selectByIdForUpdate(Long id);

    MerchantSettlementRecord selectBySettlementNo(String settlementNo);

    MerchantSettlementRecord selectByOrderNo(String orderNo);

    List<MerchantSettlementRecord> selectList(MerchantSettlementRecord query);

    List<MerchantSettlementRecord> selectByMerchantIdAndStatuses(Long merchantId, List<String> statuses);

    int insert(MerchantSettlementRecord record);

    int updateById(MerchantSettlementRecord record);

    void batchTransfer(List<Long> ids);

    void batchMarkArrived(List<Long> ids);

    void markFailed(Long id, String failReason);

    void createSettlementForOrder(String orderNo, Long merchantId, Long storeId, BigDecimal orderAmount,
                                  BigDecimal merchantAmount, BigDecimal platformFeeAmount, String title);

    void processWaitingTransfer(int batchSize);

    List<MerchantSettlementRecord> selectWaitingTransfer(int limit);

    void handleRefundReverse(String orderNo, String failReason);

    BigDecimal sumMerchantAmountByStatus(Long merchantId, String status);

    BigDecimal sumMerchantAmountToday(Long merchantId);

    BigDecimal sumMerchantAmountThisMonth(Long merchantId);

    Integer countCompletedByMerchantId(Long merchantId);

    List<Map<String, Object>> selectDailyFlowSummary(Long merchantId, String startDate, String endDate);

    List<MerchantSettlementRecord> selectDailyFlowDetails(Long merchantId, String startDate, String endDate, Integer limit);
}
