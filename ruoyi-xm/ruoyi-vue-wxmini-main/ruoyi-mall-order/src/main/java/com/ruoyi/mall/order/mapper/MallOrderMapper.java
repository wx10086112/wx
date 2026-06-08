package com.ruoyi.mall.order.mapper;

import com.ruoyi.mall.order.domain.MallOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MallOrderMapper {

    MallOrder selectMallOrderById(Long id);

    MallOrder selectMallOrderByOrderNo(String orderNo);

    MallOrder selectMallOrderByOrderNoForUpdate(String orderNo);

    List<MallOrder> selectMallOrderList(MallOrder mallOrder);

    List<MallOrder> selectMallOrderByMerchantId(Long merchantId);

    int insertMallOrder(MallOrder mallOrder);

    int updateMallOrder(MallOrder mallOrder);

    int markOrderPaid(@Param("orderNo") String orderNo, @Param("payTime") Date payTime);

    int cancelPendingOrder(@Param("orderNo") String orderNo, @Param("cancelTime") Date cancelTime);

    int markOrderWriteOffCompleted(@Param("id") Long id,
                                   @Param("merchantId") Long merchantId,
                                   @Param("operatorId") Long operatorId,
                                   @Param("writeOffTime") Date writeOffTime);

    int markOrderRefunded(@Param("orderNo") String orderNo, @Param("refundTime") Date refundTime);

    int deleteMallOrderById(Long id);

    int deleteMallOrderByIds(Long[] ids);

    int countOrderByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);

    @Select("SELECT COUNT(1) FROM merchant WHERE id = #{merchantId} AND distributor_id = #{distributorId} AND del_flag = '0'")
    int countMerchantByDistributor(@Param("merchantId") Long merchantId, @Param("distributorId") Long distributorId);

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

    @Select({
            "<script>",
            "SELECT COUNT(*)",
            "FROM mall_order o",
            "LEFT JOIN merchant m ON o.merchant_id = m.id",
            "WHERE o.del_flag = '0' AND DATE(o.create_time) = CURDATE()",
            "<if test='merchantId != null'>AND o.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "</script>"
    })
    int countTodayOrdersScoped(@Param("merchantId") Long merchantId, @Param("distributorId") Long distributorId);

    @Select("SELECT IFNULL(SUM(pay_amount), 0) FROM mall_order WHERE DATE(create_time) = CURDATE() AND status NOT IN (0, 5)")
    BigDecimal sumTodayAmount();

    @Select({
            "<script>",
            "SELECT IFNULL(SUM(o.pay_amount), 0)",
            "FROM mall_order o",
            "LEFT JOIN merchant m ON o.merchant_id = m.id",
            "WHERE o.del_flag = '0' AND DATE(o.create_time) = CURDATE() AND o.status NOT IN (0, 5)",
            "<if test='merchantId != null'>AND o.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "</script>"
    })
    BigDecimal sumTodayAmountScoped(@Param("merchantId") Long merchantId, @Param("distributorId") Long distributorId);

    @Select("SELECT IFNULL(SUM(pay_amount), 0) FROM mall_order WHERE status NOT IN (0, 5)")
    BigDecimal sumTotalFlow();

    @Select({
            "<script>",
            "SELECT IFNULL(SUM(o.pay_amount), 0)",
            "FROM mall_order o",
            "LEFT JOIN merchant m ON o.merchant_id = m.id",
            "WHERE o.del_flag = '0' AND o.status NOT IN (0, 5)",
            "<if test='merchantId != null'>AND o.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "</script>"
    })
    BigDecimal sumTotalFlowScoped(@Param("merchantId") Long merchantId, @Param("distributorId") Long distributorId);

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

    @Select({
            "<script>",
            "SELECT DATE(o.create_time) AS date, COUNT(*) AS orderCount,",
            "IFNULL(SUM(o.pay_amount), 0) AS totalAmount",
            "FROM mall_order o",
            "LEFT JOIN merchant m ON o.merchant_id = m.id",
            "WHERE o.del_flag = '0' AND DATE(o.create_time) >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)",
            "<if test='merchantId != null'>AND o.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "GROUP BY DATE(o.create_time)",
            "ORDER BY date",
            "</script>"
    })
    List<Map<String, Object>> selectTrendByDayScoped(@Param("days") int days,
                                                     @Param("merchantId") Long merchantId,
                                                     @Param("distributorId") Long distributorId);

    @Select("SELECT YEARWEEK(create_time, 1) AS weekNum, COUNT(*) AS orderCount, " +
            "IFNULL(SUM(pay_amount), 0) AS totalAmount " +
            "FROM mall_order WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{weeks} WEEK) " +
            "GROUP BY YEARWEEK(create_time, 1) ORDER BY weekNum")
    List<Map<String, Object>> selectTrendByWeek(@Param("weeks") int weeks);

    @Select({
            "<script>",
            "SELECT YEARWEEK(o.create_time, 1) AS weekNum, COUNT(*) AS orderCount,",
            "IFNULL(SUM(o.pay_amount), 0) AS totalAmount",
            "FROM mall_order o",
            "LEFT JOIN merchant m ON o.merchant_id = m.id",
            "WHERE o.del_flag = '0' AND o.create_time >= DATE_SUB(CURDATE(), INTERVAL #{weeks} WEEK)",
            "<if test='merchantId != null'>AND o.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "GROUP BY YEARWEEK(o.create_time, 1)",
            "ORDER BY weekNum",
            "</script>"
    })
    List<Map<String, Object>> selectTrendByWeekScoped(@Param("weeks") int weeks,
                                                      @Param("merchantId") Long merchantId,
                                                      @Param("distributorId") Long distributorId);

    @Select("SELECT MONTH(create_time) AS monthNum, COUNT(*) AS orderCount, " +
            "IFNULL(SUM(pay_amount), 0) AS totalAmount " +
            "FROM mall_order WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{months} MONTH) " +
            "GROUP BY YEAR(create_time), MONTH(create_time) ORDER BY YEAR(create_time), MONTH(create_time)")
    List<Map<String, Object>> selectTrendByMonth(@Param("months") int months);

    @Select({
            "<script>",
            "SELECT MONTH(o.create_time) AS monthNum, COUNT(*) AS orderCount,",
            "IFNULL(SUM(o.pay_amount), 0) AS totalAmount",
            "FROM mall_order o",
            "LEFT JOIN merchant m ON o.merchant_id = m.id",
            "WHERE o.del_flag = '0' AND o.create_time >= DATE_SUB(CURDATE(), INTERVAL #{months} MONTH)",
            "<if test='merchantId != null'>AND o.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "GROUP BY YEAR(o.create_time), MONTH(o.create_time)",
            "ORDER BY YEAR(o.create_time), MONTH(o.create_time)",
            "</script>"
    })
    List<Map<String, Object>> selectTrendByMonthScoped(@Param("months") int months,
                                                       @Param("merchantId") Long merchantId,
                                                       @Param("distributorId") Long distributorId);

    @Select("SELECT YEAR(create_time) AS yearNum, COUNT(*) AS orderCount, " +
            "IFNULL(SUM(pay_amount), 0) AS totalAmount " +
            "FROM mall_order WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{years} YEAR) " +
            "GROUP BY YEAR(create_time) ORDER BY yearNum")
    List<Map<String, Object>> selectTrendByYear(@Param("years") int years);

    @Select({
            "<script>",
            "SELECT YEAR(o.create_time) AS yearNum, COUNT(*) AS orderCount,",
            "IFNULL(SUM(o.pay_amount), 0) AS totalAmount",
            "FROM mall_order o",
            "LEFT JOIN merchant m ON o.merchant_id = m.id",
            "WHERE o.del_flag = '0' AND o.create_time >= DATE_SUB(CURDATE(), INTERVAL #{years} YEAR)",
            "<if test='merchantId != null'>AND o.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "GROUP BY YEAR(o.create_time)",
            "ORDER BY yearNum",
            "</script>"
    })
    List<Map<String, Object>> selectTrendByYearScoped(@Param("years") int years,
                                                      @Param("merchantId") Long merchantId,
                                                      @Param("distributorId") Long distributorId);

    @Select({
            "<script>",
            "SELECT COUNT(*)",
            "FROM mall_order o",
            "LEFT JOIN merchant m ON o.merchant_id = m.id",
            "WHERE o.del_flag = '0' AND o.status = #{status}",
            "<if test='merchantId != null'>AND o.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "</script>"
    })
    int countByStatusScoped(@Param("status") Integer status,
                            @Param("merchantId") Long merchantId,
                            @Param("distributorId") Long distributorId);

    @Select({
            "<script>",
            "SELECT o.status, COUNT(*) AS cnt",
            "FROM mall_order o",
            "LEFT JOIN merchant m ON o.merchant_id = m.id",
            "WHERE o.del_flag = '0'",
            "<if test='merchantId != null'>AND o.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "GROUP BY o.status",
            "</script>"
    })
    List<Map<String, Object>> selectOrderStatsByStatusScoped(@Param("merchantId") Long merchantId,
                                                             @Param("distributorId") Long distributorId);

    @Select({
            "<script>",
            "SELECT COUNT(*) AS totalOrders,",
            "IFNULL(SUM(o.pay_amount), 0) AS totalAmount,",
            "IFNULL(SUM(o.commission), 0) AS totalCommission,",
            "IFNULL(SUM(o.merchant_income), 0) AS totalMerchantIncome",
            "FROM mall_order o",
            "LEFT JOIN merchant m ON o.merchant_id = m.id",
            "WHERE o.del_flag = '0' AND o.status NOT IN (0, 5)",
            "<if test='merchantId != null'>AND o.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "</script>"
    })
    Map<String, Object> selectSalesStatsScoped(@Param("merchantId") Long merchantId,
                                               @Param("distributorId") Long distributorId);

    @Select({
            "<script>",
            "SELECT DATE(o.create_time) AS date, o.status, COUNT(*) AS count",
            "FROM mall_order o",
            "LEFT JOIN merchant m ON o.merchant_id = m.id",
            "WHERE o.del_flag = '0' AND DATE(o.create_time) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)",
            "<if test='merchantId != null'>AND o.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "GROUP BY DATE(o.create_time), o.status",
            "ORDER BY date",
            "</script>"
    })
    List<Map<String, Object>> selectDailyOrderStatsByStatusScoped(@Param("merchantId") Long merchantId,
                                                                  @Param("distributorId") Long distributorId);

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

    // ---- 数据分析：商家排行 ----

    List<Map<String, Object>> selectMerchantRankForAnalysis(@Param("keyword") String keyword,
                                                             @Param("sortBy") String sortBy,
                                                             @Param("distributorId") Long distributorId,
                                                             @Param("offset") int offset,
                                                             @Param("pageSize") int pageSize);

    int countMerchantRankForAnalysis(@Param("keyword") String keyword,
                                     @Param("distributorId") Long distributorId);

    // ---- 数据分析：每日订单状态明细 ----

    @Select("SELECT DATE(create_time) AS date, status, COUNT(*) AS count " +
            "FROM mall_order " +
            "WHERE DATE(create_time) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY DATE(create_time), status ORDER BY date")
    List<Map<String, Object>> selectDailyOrderStatsByStatus();
}
