package com.ruoyi.wxmini.service;

import java.util.List;
import java.util.Map;

public interface IDashboardService {
    Map<String, Object> selectDashboardStats();
    Map<String, Object> selectTrendData();
    List<Map<String, Object>> selectOrderStatusData();
    List<Map<String, Object>> selectHotProducts();
    List<Map<String, Object>> selectMerchantRank();
}
