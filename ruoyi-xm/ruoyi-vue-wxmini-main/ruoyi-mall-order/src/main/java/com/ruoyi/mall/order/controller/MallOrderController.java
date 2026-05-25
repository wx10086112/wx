package com.ruoyi.mall.order.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.OrderItem;
import com.ruoyi.mall.order.service.IMallOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mall/order")
public class MallOrderController extends BaseController {

    @Autowired
    private IMallOrderService mallOrderService;

    @PreAuthorize("@ss.hasPermi('mall:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(MallOrder query) {
        startPage();
        List<MallOrder> list = mallOrderService.selectMallOrderList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:order:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        MallOrder order = mallOrderService.selectMallOrderById(id);
        List<OrderItem> items = mallOrderService.selectOrderItemListByOrderId(id);
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);
        return success(data);
    }

    @PreAuthorize("@ss.hasPermi('mall:order:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MallOrder mallOrder) {
        return toAjax(mallOrderService.updateMallOrder(mallOrder));
    }
}
