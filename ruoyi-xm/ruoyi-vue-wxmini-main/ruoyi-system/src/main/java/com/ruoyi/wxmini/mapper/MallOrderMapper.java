package com.ruoyi.wxmini.mapper;

import com.ruoyi.wxmini.domain.MallOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MallOrderMapper {
    MallOrder selectMallOrderById(Long id);
    MallOrder selectMallOrderByOrderNo(String orderNo);
    List<MallOrder> selectMallOrderList(MallOrder mallOrder);
    List<MallOrder> selectMallOrderByMerchantId(Long merchantId);
    int insertMallOrder(MallOrder mallOrder);
    int updateMallOrder(MallOrder mallOrder);
    int deleteMallOrderById(Long id);
    int deleteMallOrderByIds(Long[] ids);
    int countOrderByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);

    @Select("SELECT COALESCE(SUM(total_amount),0) as totalSales, COUNT(*) as totalOrders, " +
            "COALESCE(AVG(total_amount),0) as avgOrderAmount FROM mall_order WHERE status IN (1,2)")
    Map<String, Object> selectSalesStats();

    @Select("SELECT status, COUNT(*) as cnt FROM mall_order GROUP BY status")
    List<Map<String, Object>> selectOrderStatsByStatus();

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') as date, COUNT(*) as count, " +
            "COALESCE(SUM(total_amount),0) as amount FROM mall_order " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') ORDER BY date")
    List<Map<String, Object>> selectDailyOrderStats();

    @Select("SELECT COUNT(*) FROM mall_order WHERE status = #{status}")
    int countByStatus(@Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM mall_order WHERE DATE(create_time) = CURDATE()")
    int countTodayOrders();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM mall_order WHERE DATE(create_time) = CURDATE()")
    BigDecimal sumTodayAmount();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM mall_order WHERE status IN (1, 2, 3)")
    BigDecimal sumTotalFlow();

    @Select("SELECT DATE_FORMAT(create_time, '%m-%d') as date, COUNT(*) as count, " +
            "COALESCE(SUM(total_amount), 0) as amount, " +
            "COUNT(CASE WHEN status = 3 THEN 1 END) as completedCount " +
            "FROM mall_order WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE_FORMAT(create_time, '%m-%d') ORDER BY MIN(create_time)")
    List<Map<String, Object>> selectDailyStatsForWeek();
}