package com.ruoyi.mall.finance.mapper;

import com.ruoyi.mall.finance.domain.PlatformTransferRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlatformTransferRecordMapper {

    PlatformTransferRecord selectById(Long id);

    PlatformTransferRecord selectByTransferNo(String transferNo);

    PlatformTransferRecord selectBySettlementNo(String settlementNo);

    PlatformTransferRecord selectActiveBySettlementNo(@Param("settlementNo") String settlementNo);

    List<PlatformTransferRecord> selectList(PlatformTransferRecord query);

    List<PlatformTransferRecord> selectTimeoutTransfers(@Param("timeoutMinutes") int timeoutMinutes, @Param("limit") int limit);

    int insert(PlatformTransferRecord record);

    int updateById(PlatformTransferRecord record);
}
