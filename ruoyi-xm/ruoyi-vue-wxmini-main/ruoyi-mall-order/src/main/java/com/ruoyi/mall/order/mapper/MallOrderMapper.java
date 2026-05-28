package com.ruoyi.mall.order.mapper;

import com.ruoyi.mall.order.domain.MallOrder;
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

    @Select("SELECT COUNT(*) AS totalOrders, IFNULL(SUM(pay_amount), 0) AS totalAmount, " +
            "IFNULL(SUM(commission), 0) AS totalCommission, IFNULL(SUM(merchant_income), 0) AS totalMerchantIncome " +
            "FROM mall_order WHERE status NOT IN (0, 5)")
    Map<String, Object> selectSalesStats();

    @Select("SELECT status, COUNT(*) AS cnt FROM mall_order GROUP BY status")
    List<Map<String, Object>> selectOrderStatsByStatus();

    @Select("SELECT DATE(create_time) AS date, COUNT(*) AS count, IFNULL(SUM(pay_amount), 0) AS amount " +
            "FROM mall_order WHERE DATE(create_time) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> selectDailyOrderStats();

    @Select("SELECT COUNT(*) FROM mall_order WHERE status = #{status}")
    int countByStatus(@Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM mall_order WHERE DATE(create_time) = CURDATE()")
    int countTodayOrders();

    @Select("SELECT IFNULL(SUM(pay_amount), 0) FROM mall_order WHERE DATE(create_time) = CURDATE() AND status NOT IN (0, 5)")
    BigDecimal sumTodayAmount();

    @Select("SELECT IFNULL(SUM(pay_amount), 0) FROM mall_order WHERE status NOT IN (0, 5)")
    BigDecimal sumTotalFlow();

    @Select("SELECT DATE(create_time) AS date, COUNT(*) AS orderCount, " +
            "IFNULL(SUM(pay_amount), 0) AS totalAmount, IFNULL(SUM(commission), 0) AS totalCommission " +
            "FROM mall_order WHERE DATE(create_time) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> selectDailyStatsForWeek();

    MallOrder selectOrderByWriteOffCode(String code);

    @Select("SELECT DATE(create_time) AS date, COUNT(*) AS orderCount, " +
            "IFNULL(SUM(pay_amount), 0) AS totalAmount " +
            "FROM mall_order WHERE DATE(create_time) >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> selectTrendByDay(@Param("days") int days);

    @Select("SELECT YEARWEEK(create_time, 1) AS weekNum, COUNT(*) AS orderCount, " +
            "IFNULL(SUM(pay_amount), 0) AS totalAmount " +
            "FROM mall_order WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{weeks} WEEK) " +
            "GROUP BY YEARWEEK(create_time, 1) ORDER BY weekNum")
    List<Map<String, Object>> selectTrendByWeek(@Param("weeks") int weeks);

    @Select("SELECT MONTH(create_time) AS monthNum, COUNT(*) AS orderCount, " +
            "IFNULL(SUM(pay_amount), 0) AS totalAmount " +
            "FROM mall_order WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{months} MONTH) " +
            "GROUP BY YEAR(create_time), MONTH(create_time) ORDER BY YEAR(create_time), MONTH(create_time)")
    List<Map<String, Object>> selectTrendByMonth(@Param("months") int months);

    @Select("SELECT YEAR(create_time) AS yearNum, COUNT(*) AS orderCount, " +
            "IFNULL(SUM(pay_amount), 0) AS totalAmount " +
            "FROM mall_order WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{years} YEAR) " +
            "GROUP BY YEAR(create_time) ORDER BY yearNum")
    List<Map<String, Object>> selectTrendByYear(@Param("years") int years);

    // ---- S-2: SQL aggregation for settlement overview ----

    Long countCompletedOrdersByMerchantId(@Param("merchantId") Long merchantId);

    BigDecimal sumTodayIncomeByMerchantId(@Param("merchantId") Long merchantId);

    BigDecimal sumMonthIncomeByMerchantId(@Param("merchantId") Long merchantId);

    BigDecimal sumPayAmountByMerchantIdAndStatuses(@Param("merchantId") Long merchantId,
                                                    @Param("statuses") List<Integer> statuses);

    List<MallOrder> selectRecentCompletedOrdersByMerchantId(@Param("merchantId") Long merchantId,
                                                             @Param("limit") int limit);

    Long countByMerchantIdAndStatusIn(@Param("merchantId") Long merchantId,
                                      @Param("statuses") List<Integer> statuses);

    BigDecimal sumTodaySalesByMerchantId(@Param("merchantId") Long merchantId);
}
