package com.ruoyi.wxmini.service.impl;

import com.ruoyi.wxmini.mapper.*;
import com.ruoyi.wxmini.service.IDashboardService;
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

    @Autowired
    private TransactionRecordMapper transactionRecordMapper;

    @Override
    public Map<String, Object> selectDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        // 今日交易额、总流水、今日订单数、商家数、用户数、今日新增用户
        // 通过 Mapper 直接执行聚合查询
        stats.put("todayAmount", BigDecimal.ZERO);
        stats.put("totalFlow", BigDecimal.ZERO);
        stats.put("todayOrders", 0);
        stats.put("merchantCount", 0);
        stats.put("userTotal", 0);
        stats.put("userTodayNew", 0);
        return stats;
    }

    @Override
    public Map<String, Object> selectTrendData() {
        Map<String, Object> trend = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        Calendar cal = Calendar.getInstance();

        List<String> dates = new ArrayList<>();
        List<Integer> orderCounts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        List<Integer> completedCounts = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, -i);
            dates.add(sdf.format(cal.getTime()));
            orderCounts.add(0);
            amounts.add(BigDecimal.ZERO);
            completedCounts.add(0);
        }

        trend.put("dates", dates);
        trend.put("orderCounts", orderCounts);
        trend.put("amounts", amounts);
        trend.put("completedCounts", completedCounts);
        return trend;
    }

    @Override
    public List<Map<String, Object>> selectOrderStatusData() {
        // 返回各状态订单数量
        List<Map<String, Object>> result = new ArrayList<>();
        String[] statusNames = {"待支付", "已支付", "已使用", "已完成", "已退款", "已取消"};
        for (int i = 0; i < statusNames.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", i);
            item.put("name", statusNames[i]);
            item.put("count", 0);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> selectHotProducts() {
        // 按销量排序的商品排行
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> selectMerchantRank() {
        // 按收入排序的商家排行
        return new ArrayList<>();
    }
}
