package com.ruoyi.wxmini.controller.merchant;

import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.wxmini.domain.MallOrder;
import com.ruoyi.wxmini.domain.RefundRecord;
import com.ruoyi.wxmini.service.IMallOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mall/after-sale")
public class MallAfterSaleController extends BaseController {

    @Autowired
    private IMallOrderService mallOrderService;

    @PreAuthorize("@ss.hasPermi('mall:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(RefundRecord query) {
        startPage();
        List<RefundRecord> list = mallOrderService.selectRefundList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:order:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        RefundRecord refund = mallOrderService.selectRefundById(id);
        Map<String, Object> data = new HashMap<>();
        data.put("refund", refund);
        if (refund != null) {
            MallOrder order = mallOrderService.selectMallOrderById(null);
            // 通过订单号查找关联订单
            MallOrder query = new MallOrder();
            query.setOrderNo(refund.getOrderNo());
            List<MallOrder> orders = mallOrderService.selectMallOrderList(query);
            if (!orders.isEmpty()) {
                data.put("order", orders.get(0));
            }
        }
        return success(data);
    }

    @PreAuthorize("@ss.hasPermi('mall:order:edit')")
    @PostMapping("/handle/{id}/{status}")
    public AjaxResult handle(@PathVariable Long id, @PathVariable Integer status,
                             @RequestBody(required = false) Map<String, String> body) {
        String rejectReason = body != null ? body.get("rejectReason") : "";
        return toAjax(mallOrderService.handleRefund(id, status, getUsername(), rejectReason));
    }
}
