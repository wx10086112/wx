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

    @Select({"<script>",
            "SELECT COALESCE(SUM(t.amount), 0) FROM transaction_record t",
            "LEFT JOIN merchant m ON t.merchant_id = m.id",
            "WHERE t.del_flag = '0' AND t.type = #{type} AND DATE(t.create_time) = CURDATE()",
            "<if test='merchantId != null'>AND t.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "</script>"})
    BigDecimal sumTodayByTypeScoped(@Param("type") Integer type, @Param("merchantId") Long merchantId, @Param("distributorId") Long distributorId);

    @Select({"<script>",
            "SELECT COALESCE(SUM(t.amount), 0) FROM transaction_record t",
            "LEFT JOIN merchant m ON t.merchant_id = m.id",
            "WHERE t.del_flag = '0' AND t.type = #{type}",
            "AND YEAR(t.create_time) = YEAR(NOW()) AND MONTH(t.create_time) = MONTH(NOW())",
            "<if test='merchantId != null'>AND t.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "</script>"})
    BigDecimal sumMonthByTypeScoped(@Param("type") Integer type, @Param("merchantId") Long merchantId, @Param("distributorId") Long distributorId);

    @Select({"<script>",
            "SELECT DATE_FORMAT(t.create_time, '%Y-%m') AS month, SUM(t.amount) AS totalAmount, t.type",
            "FROM transaction_record t",
            "LEFT JOIN merchant m ON t.merchant_id = m.id",
            "WHERE t.del_flag = '0'",
            "<if test='merchantId != null'>AND t.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "GROUP BY DATE_FORMAT(t.create_time, '%Y-%m'), t.type ORDER BY month DESC",
            "</script>"})
    List<Map> selectMonthlyReportScoped(@Param("merchantId") Long merchantId, @Param("distributorId") Long distributorId);
}
