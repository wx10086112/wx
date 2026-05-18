package com.ruoyi.mall.order.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.service.IMallOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mall/after-sale")
public class MallAfterSaleController extends BaseController {

    @Autowired
    private IMallOrderService mallOrderService;

    @PreAuthorize("@ss.hasPermi('mall:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(RefundRecord query) {
        startPage();
        return getDataTable(mallOrderService.selectRefundList(query));
    }

    @PreAuthorize("@ss.hasPermi('mall:order:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(mallOrderService.selectRefundById(id));
    }

    @PreAuthorize("@ss.hasPermi('mall:order:edit')")
    @PostMapping("/handle/{id}/{status}")
    public AjaxResult handle(@PathVariable Long id, @PathVariable Integer status,
                              @RequestParam(required = false) String operator,
                              @RequestParam(required = false) String rejectReason) {
        return toAjax(mallOrderService.handleRefund(id, status, operator, rejectReason));
    }
}
