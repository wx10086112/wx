package com.ruoyi.wxmini.controller.merchant;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.wxmini.domain.MallUser;
import com.ruoyi.wxmini.mapper.MallUserMapper;
import com.ruoyi.wxmini.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mall/dashboard")
public class MallDashboardController extends BaseController {

    @Autowired
    private IDashboardService dashboardService;

    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/stats")
    public AjaxResult stats() {
        return success(dashboardService.selectDashboardStats());
    }

    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/trend")
    public AjaxResult trend() {
        return success(dashboardService.selectTrendData());
    }

    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/order-status")
    public AjaxResult orderStatus() {
        return success(dashboardService.selectOrderStatusData());
    }

    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/hot-products")
    public AjaxResult hotProducts() {
        return success(dashboardService.selectHotProducts());
    }

    @PreAuthorize("@ss.hasPermi('mall:dashboard:list')")
    @GetMapping("/merchant-rank")
    public AjaxResult merchantRank() {
        return success(dashboardService.selectMerchantRank());
    }
}
