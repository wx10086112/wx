package com.ruoyi.mall.finance.mapper;

import com.ruoyi.mall.finance.domain.TransactionRecord;
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

    @Select("SELECT COALESCE(SUM(amount), 0) FROM transaction_record WHERE type = #{type} AND YEAR(create_time) = YEAR(NOW()) AND MONTH(create_time) = MONTH(NOW())")
    BigDecimal sumMonthByType(@Param("type") Integer type);

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, SUM(amount) AS totalAmount, type FROM transaction_record GROUP BY DATE_FORMAT(create_time, '%Y-%m'), type ORDER BY month DESC")
    List<Map> selectMonthlyReport();
}
