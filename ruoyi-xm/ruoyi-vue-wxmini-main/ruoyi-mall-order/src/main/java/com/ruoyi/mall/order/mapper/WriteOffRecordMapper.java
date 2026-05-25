package com.ruoyi.mall.order.mapper;

import com.ruoyi.mall.order.domain.WriteOffRecord;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface WriteOffRecordMapper {

    WriteOffRecord selectWriteOffRecordById(Long id);

    List<WriteOffRecord> selectWriteOffRecordList(WriteOffRecord query);

    List<WriteOffRecord> selectByMerchantId(@Param("merchantId") Long merchantId);

    WriteOffRecord selectByWriteOffCode(String writeOffCode);

    int insertWriteOffRecord(WriteOffRecord record);
}
