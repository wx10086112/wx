package com.ruoyi.wxmini.mapper;

import com.ruoyi.wxmini.domain.TransactionRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionRecordMapper {
    TransactionRecord selectTransactionRecordById(Long id);
    List<TransactionRecord> selectTransactionRecordList(TransactionRecord transactionRecord);
    List<TransactionRecord> selectTransactionRecordByMerchantId(Long merchantId);
    int insertTransactionRecord(TransactionRecord transactionRecord);
    int updateTransactionRecord(TransactionRecord transactionRecord);
    int deleteTransactionRecordById(Long id);
    int deleteTransactionRecordByIds(Long[] ids);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM transaction_record WHERE type = #{type}")
    BigDecimal sumAmountByType(@Param("type") Integer type);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM transaction_record WHERE type = #{type} AND DATE(create_time) = CURDATE()")
    BigDecimal sumTodayByType(@Param("type") Integer type);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM transaction_record WHERE type = #{type} AND DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')")
    BigDecimal sumMonthByType(@Param("type") Integer type);

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') as month, " +
            "COALESCE(SUM(CASE WHEN type=1 THEN amount END),0) as revenue, " +
            "COALESCE(SUM(CASE WHEN type=5 THEN amount END),0) as commission, " +
            "COUNT(CASE WHEN type=1 THEN 1 END) as orders " +
            "FROM transaction_record WHERE type IN (1,5) " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') ORDER BY month DESC LIMIT 12")
    List<Map<String, Object>> selectMonthlyReport();
}
