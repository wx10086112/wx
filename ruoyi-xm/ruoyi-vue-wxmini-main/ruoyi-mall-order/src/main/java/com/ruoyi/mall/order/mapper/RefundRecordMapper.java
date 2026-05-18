package com.ruoyi.mall.order.mapper;

import com.ruoyi.mall.order.domain.RefundRecord;
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

    @Select("SELECT IFNULL(SUM(refund_amount), 0) FROM refund_record WHERE status = 2")
    BigDecimal sumRefundTotal();
}
