package com.ruoyi.mall.finance.mapper;

import com.ruoyi.mall.finance.domain.DistributorSettlementRecord;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface DistributorSettlementRecordMapper {

    DistributorSettlementRecord selectById(Long id);

    DistributorSettlementRecord selectByIdForUpdate(Long id);

    DistributorSettlementRecord selectBySettlementNo(String settlementNo);

    List<DistributorSettlementRecord> selectList(DistributorSettlementRecord query);

    List<DistributorSettlementRecord> selectByDistributorIdAndStatuses(@Param("distributorId") Long distributorId, @Param("statuses") List<String> statuses);

    int insert(DistributorSettlementRecord record);

    int updateById(DistributorSettlementRecord record);

    BigDecimal sumAmountByStatus(@Param("distributorId") Long distributorId, @Param("status") String status);

    BigDecimal sumAmountByDistributorId(@Param("distributorId") Long distributorId);
}
