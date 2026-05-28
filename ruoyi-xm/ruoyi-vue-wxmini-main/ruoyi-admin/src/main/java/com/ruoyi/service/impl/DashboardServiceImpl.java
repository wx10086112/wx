package com.ruoyi.service.impl;

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
            if (status == 2) completedOrders = cnt;
            if (status == 3) refundOrders = cnt;
            if (status == 5) abnormalOrders = cnt;
        }

        List<Map<String, Object>> dailyData = mallOrderMapper.selectDailyOrderStats();

        stats.put("totalOrders", totalOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("refundOrders", refundOrders);
        stats.put("abnormalOrders", abnormalOrders);
        stats.put("dailyData", dailyData);
        return stats;
    }
}
