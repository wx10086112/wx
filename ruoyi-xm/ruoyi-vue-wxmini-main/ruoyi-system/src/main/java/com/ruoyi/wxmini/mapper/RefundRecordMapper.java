package com.ruoyi.wxmini.mapper;

import com.ruoyi.wxmini.domain.RefundRecord;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.util.List;

public interface RefundRecordMapper {
    RefundRecord selectRefundRecordById(Long id);
    RefundRecord selectRefundRecordByRefundNo(String refundNo);
    List<RefundRecord> selectRefundRecordList(RefundRecord refundRecord);
    int insertRefundRecord(RefundRecord refundRecord);
    int updateRefundRecord(RefundRecord refundRecord);
    int deleteRefundRecordById(Long id);
    int deleteRefundRecordByIds(Long[] ids);

    @Select("SELECT COALESCE(SUM(refund_amount), 0) FROM refund_record WHERE status IN (1,2)")
    BigDecimal sumRefundTotal();
}
