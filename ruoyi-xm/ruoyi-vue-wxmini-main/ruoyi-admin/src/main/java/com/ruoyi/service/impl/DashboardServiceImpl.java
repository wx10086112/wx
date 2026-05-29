package com.ruoyi.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mall.common.service.IDashboardService;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.merchant.mapper.MerchantMapper;
import com.ruoyi.mall.user.mapper.MallUserMapper;
import com.ruoyi.mall.product.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DashboardServiceImpl implements IDashboardService {

    @Autowired
    private MallOrderMapper mallOrderMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private MallUserMapper mallUserMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Map<String, Object> selectDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayAmount", mallOrderMapper.sumTodayAmount());
        stats.put("totalFlow", mallOrderMapper.sumTotalFlow());
        stats.put("todayOrders", mallOrderMapper.countTodayOrders());
        stats.put("merchantCount", merchantMapper.countActiveMerchant());
        stats.put("userTotal", mallUserMapper.countTotal());
        stats.put("userTodayNew", mallUserMapper.countTodayNew());
        return stats;
    }

    @Override
    public Map<String, Object> selectTrendData() {
        return selectTrendData("day");
    }

    @Override
    public Map<String, Object> selectTrendData(String range) {
        if (range == null || range.isEmpty()) {
            range = "day";
        }
        switch (range) {
            case "week": return selectTrendByWeek();
            case "month": return selectTrendByMonth();
            case "year": return selectTrendByYear();
            default: return selectTrendByDay();
        }
    }

    private Map<String, Object> selectTrendByDay() {
        Map<String, Object> trend = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        Calendar cal = Calendar.getInstance();

        List<String> dates = new ArrayList<>();
        Map<String, Integer> countMap = new LinkedHashMap<>();
        Map<String, BigDecimal> amountMap = new LinkedHashMap<>();

        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, -i);
            String dateStr = sdf.format(cal.getTime());
            dates.add(dateStr);
            countMap.put(dateStr, 0);
            amountMap.put(dateStr, BigDecimal.ZERO);
        }

        List<Map<String, Object>> dbData = mallOrderMapper.selectTrendByDay(7);
        SimpleDateFormat dbSdf = new SimpleDateFormat("MM-dd");
        for (Map<String, Object> row : dbData) {
            Object dateObj = row.get("date");
            String date = dbSdf.format(dateObj);
            if (countMap.containsKey(date)) {
                countMap.put(date, ((Number) row.get("orderCount")).intValue());
                amountMap.put(date, (BigDecimal) row.get("totalAmount"));
            }
        }

        List<Integer> orderCounts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        for (String d : dates) {
            orderCounts.add(countMap.get(d));
            amounts.add(amountMap.get(d));
        }

        trend.put("dates", dates);
        trend.put("orderCounts", orderCounts);
        trend.put("amounts", amounts);
        return trend;
    }

    private Map<String, Object> selectTrendByWeek() {
        Map<String, Object> trend = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> orderCounts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();

        List<Map<String, Object>> dbData = mallOrderMapper.selectTrendByWeek(7);
        for (Map<String, Object> row : dbData) {
            String week = "第" + row.get("weekNum") + "周";
            dates.add(week);
            orderCounts.add(((Number) row.get("orderCount")).intValue());
            amounts.add((BigDecimal) row.get("totalAmount"));
        }

        trend.put("dates", dates);
        trend.put("orderCounts", orderCounts);
        trend.put("amounts", amounts);
        return trend;
    }

    private Map<String, Object> selectTrendByMonth() {
        Map<String, Object> trend = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> orderCounts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();

        List<Map<String, Object>> dbData = mallOrderMapper.selectTrendByMonth(12);
        for (Map<String, Object> row : dbData) {
            String month = row.get("monthNum") + "月";
            dates.add(month);
            orderCounts.add(((Number) row.get("orderCount")).intValue());
            amounts.add((BigDecimal) row.get("totalAmount"));
        }

        trend.put("dates", dates);
        trend.put("orderCounts", orderCounts);
        trend.put("amounts", amounts);
        return trend;
    }

    private Map<String, Object> selectTrendByYear() {
        Map<String, Object> trend = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> orderCounts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();

        List<Map<String, Object>> dbData = mallOrderMapper.selectTrendByYear(5);
        for (Map<String, Object> row : dbData) {
            String year = row.get("yearNum") + "年";
            dates.add(year);
            orderCounts.add(((Number) row.get("orderCount")).intValue());
            amounts.add((BigDecimal) row.get("totalAmount"));
        }

        trend.put("dates", dates);
        trend.put("orderCounts", orderCounts);
        trend.put("amounts", amounts);
        return trend;
    }

    @Override
    public List<Map<String, Object>> selectOrderStatusData() {
        String[] statusNames = {"待支付", "已支付", "已使用", "已完成", "已退款", "已取消"};

        Map<Integer, Integer> countMap = new LinkedHashMap<>();
        for (int i = 0; i < statusNames.length; i++) {
            countMap.put(i, 0);
        }

        List<Map<String, Object>> dbData = mallOrderMapper.selectOrderStatsByStatus();
        for (Map<String, Object> row : dbData) {
            Integer status = ((Number) row.get("status")).intValue();
            Integer count = ((Number) row.get("cnt")).intValue();
            countMap.put(status, count);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < statusNames.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", i);
            item.put("name", statusNames[i]);
            item.put("count", countMap.get(i));
            result.add(item);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> selectHotProducts() {
        return (List) productMapper.selectHotProducts(5);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> selectMerchantRank() {
        return (List) merchantMapper.selectMerchantRankByIncome(5);
    }

    @Override
    public Map<String, Object> selectMerchantRankWithParams(Map<String, Object> params) {
        String keyword = (String) params.get("keyword");
        String sortBy = (String) params.get("sortBy");
        int pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        int offset = (pageNum - 1) * pageSize;

        Long distributorId = null;
        String accountType = SecurityUtils.getAccountType();
        if ("DISTRIBUTOR".equals(accountType)) {
            distributorId = SecurityUtils.getDistributorId();
        }

        int total = mallOrderMapper.countMerchantRankForAnalysis(keyword, distributorId);
        List<Map<String, Object>> rows = mallOrderMapper.selectMerchantRankForAnalysis(keyword, sortBy, distributorId, offset, pageSize);

        // 计算完成率和退款率
        for (Map<String, Object> row : rows) {
            long orders = ((Number) row.get("orders")).longValue();
            long completedOrders = ((Number) row.get("completedOrders")).longValue();
            BigDecimal sales = (BigDecimal) row.get("sales");
            BigDecimal refundAmount = (BigDecimal) row.get("refundAmount");
            row.put("completionRate", orders > 0 ? (double) completedOrders / orders : 0);
            row.put("refundRate", sales != null && sales.compareTo(BigDecimal.ZERO) > 0
                    ? refundAmount.divide(sales, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("rows", rows);
        return result;
    }

    @Override
    public Map<String, Object> selectSalesStats() {
        Map<String, Object> stats = new HashMap<>();
        Map<String, Object> sales = mallOrderMapper.selectSalesStats();
        BigDecimal totalAmount = (BigDecimal) sales.get("totalAmount");
        Long totalOrders = ((Number) sales.get("totalOrders")).longValue();

        BigDecimal avgOrderAmount = BigDecimal.ZERO;
        if (totalOrders != null && totalOrders > 0 && totalAmount != null) {
            avgOrderAmount = totalAmount.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }

        int completedCount = mallOrderMapper.countByStatus(2);
        BigDecimal conversionRate = BigDecimal.ZERO;
        if (totalOrders != null && totalOrders > 0) {
            conversionRate = BigDecimal.valueOf(completedCount)
                    .divide(BigDecimal.valueOf(totalOrders), 4, RoundingMode.HALF_UP);
        }

        stats.put("totalSales", totalAmount);
        stats.put("totalOrders", totalOrders);
        stats.put("avgOrderAmount", avgOrderAmount);
        stats.put("conversionRate", conversionRate);
        stats.put("categoryData", new ArrayList<>());

        // 近7日销售趋势
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        Calendar cal = Calendar.getInstance();
        List<String> dates = new ArrayList<>();
        Map<String, BigDecimal> trendAmountMap = new LinkedHashMap<>();
        Map<String, Integer> trendCountMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, -i);
            String dateStr = sdf.format(cal.getTime());
            dates.add(dateStr);
            trendAmountMap.put(dateStr, BigDecimal.ZERO);
            trendCountMap.put(dateStr, 0);
        }
        List<Map<String, Object>> trendDb = mallOrderMapper.selectTrendByDay(7);
        SimpleDateFormat dbSdf = new SimpleDateFormat("MM-dd");
        for (Map<String, Object> row : trendDb) {
            Object dateObj = row.get("date");
            String date = dbSdf.format(dateObj);
            if (trendAmountMap.containsKey(date)) {
                trendAmountMap.put(date, (BigDecimal) row.get("totalAmount"));
                trendCountMap.put(date, ((Number) row.get("orderCount")).intValue());
            }
        }
        List<BigDecimal> trendAmounts = new ArrayList<>();
        List<Integer> trendCounts = new ArrayList<>();
        for (String d : dates) {
            trendAmounts.add(trendAmountMap.get(d));
            trendCounts.add(trendCountMap.get(d));
        }
        Map<String, Object> trendData = new HashMap<>();
        trendData.put("dates", dates);
        trendData.put("amounts", trendAmounts);
        trendData.put("orderCounts", trendCounts);
        stats.put("trendData", trendData);

        return stats;
    }

    @Override
    public Map<String, Object> selectOrderStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Map<String, Object>> statusList = mallOrderMapper.selectOrderStatsByStatus();
        int totalOrders = 0, completedOrders = 0, refundOrders = 0, abnormalOrders = 0;
        for (Map<String, Object> item : statusList) {
            Integer status = ((Number) item.get("status")).intValue();
            int cnt = ((Number) item.get("cnt")).intValue();
            totalOrders += cnt;
            if (status == 2 || status == 3) completedOrders += cnt;
            if (status == 4) refundOrders = cnt;
            if (status == 5) abnormalOrders = cnt;
        }

        // 按日期+状态分组的每日明细
        List<Map<String, Object>> rawDaily = mallOrderMapper.selectDailyOrderStatsByStatus();
        // 按日期聚合: date -> { date, newOrders, completed, refund }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        // 初始化最近30天
        LinkedHashMap<String, Map<String, Object>> dailyMap = new LinkedHashMap<>();
        for (int i = 29; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, -i);
            String dateStr = sdf.format(cal.getTime());
            Map<String, Object> day = new HashMap<>();
            day.put("date", dateStr);
            day.put("newOrders", 0);
            day.put("completed", 0);
            day.put("refund", 0);
            dailyMap.put(dateStr, day);
        }
        // 填充数据库数据
        SimpleDateFormat dbSdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Map<String, Object> row : rawDaily) {
            Object dateObj = row.get("date");
            String date = dateObj instanceof String ? (String) dateObj : dbSdf.format(dateObj);
            Map<String, Object> day = dailyMap.get(date);
            if (day == null) continue;
            int status = ((Number) row.get("status")).intValue();
            int count = ((Number) row.get("count")).intValue();
            day.put("newOrders", ((Number) day.get("newOrders")).intValue() + count);
            if (status == 2 || status == 3) {
                day.put("completed", ((Number) day.get("completed")).intValue() + count);
            }
            if (status == 4) {
                day.put("refund", ((Number) day.get("refund")).intValue() + count);
            }
        }

        List<Map<String, Object>> dailyData = new ArrayList<>(dailyMap.values());

        stats.put("totalOrders", totalOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("refundOrders", refundOrders);
        stats.put("abnormalOrders", abnormalOrders);
        stats.put("dailyData", dailyData);
        return stats;
    }
}
