package com.ruoyi.web.controller.mall;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.wxmini.mapper.MallOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/mall/dashboard")
public class MallDashboardExtendController extends BaseController {

    @Autowired
    private MallOrderMapper mallOrderMapper;

    /**
     * 销售统计
     */
    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/sales-stats")
    public AjaxResult salesStats() {
        Map<String, Object> stats = new HashMap<>();

        Map<String, Object> sales = mallOrderMapper.selectSalesStats();
        BigDecimal totalSales = (BigDecimal) sales.get("totalSales");
        Long totalOrders = (Long) sales.get("totalOrders");
        BigDecimal avgOrderAmount = (BigDecimal) sales.get("avgOrderAmount");

        // 转换率 = 已完成订单 / 总订单
        int completedCount = mallOrderMapper.countByStatus(2);
        BigDecimal conversionRate = BigDecimal.ZERO;
        if (totalOrders != null && totalOrders > 0) {
            conversionRate = BigDecimal.valueOf(completedCount)
                    .divide(BigDecimal.valueOf(totalOrders), 4, RoundingMode.HALF_UP);
        }

        stats.put("totalSales", totalSales);
        stats.put("totalOrders", totalOrders);
        stats.put("avgOrderAmount", avgOrderAmount);
        stats.put("conversionRate", conversionRate);
        stats.put("categoryData", new ArrayList<>());
        return success(stats);
    }

    /**
     * 订单统计
     */
    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/order-stats")
    public AjaxResult orderStats() {
        Map<String, Object> stats = new HashMap<>();

        // 按状态统计
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

        // 近30天每日数据
        List<Map<String, Object>> dailyData = mallOrderMapper.selectDailyOrderStats();

        stats.put("totalOrders", totalOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("refundOrders", refundOrders);
        stats.put("abnormalOrders", abnormalOrders);
        stats.put("dailyData", dailyData);
        return success(stats);
    }
}
