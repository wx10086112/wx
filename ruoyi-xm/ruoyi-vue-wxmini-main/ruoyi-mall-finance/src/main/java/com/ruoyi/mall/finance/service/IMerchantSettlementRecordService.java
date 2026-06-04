package com.ruoyi.mall.finance.service;

import com.ruoyi.mall.finance.domain.MerchantSettlementRecord;

import java.math.BigDecimal;
import java.util.List;

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

    /**
     * 为订单生成结算记录（幂等，同一order_no不会重复生成）
     */
    void createSettlementForOrder(String orderNo, Long merchantId, Long storeId, BigDecimal payAmount, String title);

    /**
     * T+1 扫描处理
     */
    void processWaitingTransfer(int batchSize);

    /**
     * 查询待打款的结算记录（WAITING_T1 且 expected_transfer_time <= NOW）
     */
    List<MerchantSettlementRecord> selectWaitingTransfer(int limit);

    /**
     * 处理退款逆向结算
     */
    void handleRefundReverse(String orderNo, String failReason);

    BigDecimal sumMerchantAmountByStatus(Long merchantId, String status);

    BigDecimal sumMerchantAmountToday(Long merchantId);

    BigDecimal sumMerchantAmountThisMonth(Long merchantId);

    Integer countCompletedByMerchantId(Long merchantId);
}
