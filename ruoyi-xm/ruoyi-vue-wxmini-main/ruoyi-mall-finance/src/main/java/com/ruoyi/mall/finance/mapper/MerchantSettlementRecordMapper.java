package com.ruoyi.mall.finance.mapper;

import com.ruoyi.mall.finance.domain.MerchantSettlementRecord;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface MerchantSettlementRecordMapper {

    MerchantSettlementRecord selectById(Long id);

    MerchantSettlementRecord selectBySettlementNo(String settlementNo);

    MerchantSettlementRecord selectByOrderNo(String orderNo);

    List<MerchantSettlementRecord> selectList(MerchantSettlementRecord query);

    List<MerchantSettlementRecord> selectByMerchantIdAndStatuses(@Param("merchantId") Long merchantId, @Param("statuses") List<String> statuses);

    List<MerchantSettlementRecord> selectWaitingTransfer(@Param("limit") int limit);

    int insert(MerchantSettlementRecord record);

    int updateById(MerchantSettlementRecord record);

    BigDecimal sumMerchantAmountByStatus(@Param("merchantId") Long merchantId, @Param("status") String status);

    BigDecimal sumMerchantAmountToday(@Param("merchantId") Long merchantId);

    BigDecimal sumMerchantAmountThisMonth(@Param("merchantId") Long merchantId);

    Integer countCompletedByMerchantId(@Param("merchantId") Long merchantId);
}
