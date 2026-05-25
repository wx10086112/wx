package com.ruoyi.service.impl;

import com.ruoyi.mall.common.service.IDashboardService;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.merchant.mapper.MerchantMapper;
import com.ruoyi.mall.user.mapper.MallUserMapper;
import com.ruoyi.mall.product.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        Map<String, Object> trend = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        Calendar cal = Calendar.getInstance();

        List<String> dates = new ArrayList<>();
        Map<String, Integer> countMap = new HashMap<>();
        Map<String, BigDecimal> amountMap = new HashMap<>();
        Map<String, Integer> completedMap = new HashMap<>();

        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, -i);
            String dateStr = sdf.format(cal.getTime());
            dates.add(dateStr);
            countMap.put(dateStr, 0);
            amountMap.put(dateStr, BigDecimal.ZERO);
            completedMap.put(dateStr, 0);
        }

        List<Map<String, Object>> dbData = mallOrderMapper.selectDailyStatsForWeek();
        for (Map<String, Object> row : dbData) {
            Object dateObj = row.get("date");
            String date = sdf.format(dateObj);
            if (countMap.containsKey(date)) {
                countMap.put(date, ((Number) row.get("orderCount")).intValue());
                amountMap.put(date, (BigDecimal) row.get("totalAmount"));
            }
        }

        List<Integer> orderCounts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        List<Integer> completedCounts = new ArrayList<>();
        for (String d : dates) {
            orderCounts.add(countMap.get(d));
            amounts.add(amountMap.get(d));
            completedCounts.add(completedMap.get(d));
        }

        trend.put("dates", dates);
        trend.put("orderCounts", orderCounts);
        trend.put("amounts", amounts);
        trend.put("completedCounts", completedCounts);
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
}
