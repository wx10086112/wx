package com.ruoyi.web.controller.mall;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.wxmini.domain.MallOrder;
import com.ruoyi.wxmini.mapper.MallOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mall/order")
public class MallOrderExtendController extends BaseController {

    @Autowired
    private MallOrderMapper mallOrderMapper;

    /**
     * 订单列表 (支持按orderNo, merchantId, userId, status筛选)
     */
    @PreAuthorize("@ss.hasPermi('mall:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(value = "orderNo", required = false) String orderNo,
                              @RequestParam(value = "merchantId", required = false) String merchantIdStr,
                              @RequestParam(value = "userId", required = false) String userIdStr,
                              @RequestParam(value = "status", required = false) Integer status) {
        startPage();
        MallOrder query = new MallOrder();
        if (orderNo != null && !orderNo.isEmpty()) {
            query.setOrderNo(orderNo);
        }
        if (merchantIdStr != null && !merchantIdStr.isEmpty()) {
            try { query.setMerchantId(Long.parseLong(merchantIdStr)); } catch (NumberFormatException ignored) {}
        }
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try { query.setUserId(Long.parseLong(userIdStr)); } catch (NumberFormatException ignored) {}
        }
        if (status != null) {
            query.setStatus(status);
        }
        return getDataTable(mallOrderMapper.selectMallOrderList(query));
    }

    /**
     * 异常订单列表 (status=5)
     */
    @PreAuthorize("@ss.hasPermi('mall:order:list')")
    @GetMapping("/abnormal/list")
    public TableDataInfo abnormalList(@RequestParam(value = "orderNo", required = false) String orderNo) {
        startPage();
        MallOrder query = new MallOrder();
        query.setStatus(5);
        if (orderNo != null && !orderNo.isEmpty()) {
            query.setOrderNo(orderNo);
        }
        return getDataTable(mallOrderMapper.selectMallOrderList(query));
    }

    /**
     * 售后订单列表 (status=4)
     */
    @PreAuthorize("@ss.hasPermi('mall:order:list')")
    @GetMapping("/after-sale/list")
    public TableDataInfo afterSaleList(@RequestParam(value = "orderNo", required = false) String orderNo) {
        startPage();
        MallOrder query = new MallOrder();
        query.setStatus(4);
        if (orderNo != null && !orderNo.isEmpty()) {
            query.setOrderNo(orderNo);
        }
        return getDataTable(mallOrderMapper.selectMallOrderList(query));
    }

    /**
     * 订单详情
     */
    @PreAuthorize("@ss.hasPermi('mall:order:list')")
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id) {
        return success(mallOrderMapper.selectMallOrderById(id));
    }
}
