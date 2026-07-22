package com.ruoyi.mall.order.mapper;

import com.ruoyi.mall.order.domain.RefundRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface RefundRecordMapper {

    RefundRecord selectRefundRecordById(Long id);

    RefundRecord selectRefundRecordByRefundNo(String refundNo);

    List<RefundRecord> selectRefundRecordList(RefundRecord refundRecord);

    int insertRefundRecord(RefundRecord refundRecord);

    int updateRefundRecord(RefundRecord refundRecord);

    int deleteRefundRecordById(Long id);

    int deleteRefundRecordByIds(Long[] ids);

    int countActiveRefundByOrderNo(@Param("orderNo") String orderNo);

    Long countActiveRefundByMerchantId(@Param("merchantId") Long merchantId);

    int markRefundSucceeded(@Param("id") Long id, @Param("refundTime") Date refundTime);

    int markRefundAbnormalWithReason(@Param("id") Long id, @Param("reason") String reason);

    List<RefundRecord> selectRetryableApprovedRefunds(@Param("limit") int limit);

    int claimApprovedRefundAttempt(@Param("id") Long id, @Param("leaseUntil") Date leaseUntil);

    int updateRefundNoForApproved(@Param("id") Long id, @Param("refundNo") String refundNo);

    int scheduleRefundRetry(@Param("id") Long id,
                            @Param("nextRetryTime") Date nextRetryTime,
                            @Param("reason") String reason,
                            @Param("maxRetryAttempts") int maxRetryAttempts);

    @Select("SELECT IFNULL(SUM(refund_amount), 0) FROM refund_record WHERE status = 2")
    BigDecimal sumRefundTotal();
}
