package com.ruoyi.web.controller.mall;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mall/dashboard")
public class MallDashboardExtendController extends BaseController {

    @Autowired
    private IDashboardService dashboardService;

    /**
     * 工作台统计
     */
    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/stats")
    public AjaxResult stats() {
        return success(dashboardService.selectDashboardStats());
    }

    /**
     * 趋势数据
     */
    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/trend")
    public AjaxResult trend(@RequestParam(defaultValue = "day") String range) {
        return success(dashboardService.selectTrendData(range));
    }

    /**
     * 商家排行
     */
    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/merchant-rank")
    public AjaxResult merchantRank() {
        return success(dashboardService.selectMerchantRank());
    }

    /**
     * 销售统计
     */
    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/sales-stats")
    public AjaxResult salesStats() {
        return success(dashboardService.selectSalesStats());
    }

    /**
     * 订单状态分布
     */
    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/order-status")
    public AjaxResult orderStatus() {
        return success(dashboardService.selectOrderStatusData());
    }

    /**
     * 热销商品
     */
    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/hot-products")
    public AjaxResult hotProducts() {
        return success(dashboardService.selectHotProducts());
    }

    /**
     * 订单统计
     */
    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/order-stats")
    public AjaxResult orderStats() {
        return success(dashboardService.selectOrderStats());
    }
}
