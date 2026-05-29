package com.ruoyi.mall.common.service;

import java.util.List;
import java.util.Map;

public interface IDashboardService {
    Map<String, Object> selectDashboardStats();
    Map<String, Object> selectTrendData();
    Map<String, Object> selectTrendData(String range);
    List<Map<String, Object>> selectOrderStatusData();
    List<Map<String, Object>> selectHotProducts();
    List<Map<String, Object>> selectMerchantRank();
    Map<String, Object> selectMerchantRankWithParams(Map<String, Object> params);
    Map<String, Object> selectSalesStats();
    Map<String, Object> selectOrderStats();
}
