package com.ruoyi.mall.order.controller;

import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.MallDataScopeHelper;
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

    @DataScopeBiz(merchantAlias = "r", distributorAlias = "m")
    @PreAuthorize("@ss.hasPermi('mall:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(RefundRecord query) {
        startPage();
        return getDataTable(mallOrderService.selectRefundList(query));
    }

    @PreAuthorize("@ss.hasPermi('mall:order:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        RefundRecord refund = mallOrderService.selectRefundById(id);
        AjaxResult denied = checkRefundAccess(refund);
        if (denied != null) {
            return denied;
        }
        return success(refund);
    }

    @PreAuthorize("@ss.hasPermi('mall:order:edit')")
    @PostMapping("/handle/{id}/{status}")
    public AjaxResult handle(@PathVariable Long id, @PathVariable Integer status,
                              @RequestParam(required = false) String operator,
                              @RequestParam(required = false) String rejectReason) {
        RefundRecord refund = mallOrderService.selectRefundById(id);
        AjaxResult denied = checkRefundAccess(refund);
        if (denied != null) {
            return denied;
        }
        return toAjax(mallOrderService.handleRefund(id, status, operator, rejectReason));
    }

    private AjaxResult checkRefundAccess(RefundRecord refund) {
        if (refund == null) {
            return AjaxResult.error("售后记录不存在");
        }
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null && !effMerchantId.equals(refund.getMerchantId())) {
            return AjaxResult.error("无权限查看该售后记录");
        }
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId == null) {
            return null;
        }
        if (!mallOrderService.isMerchantAccessibleByDistributor(refund.getMerchantId(), effDistributorId)) {
            return AjaxResult.error("无权限查看该售后记录");
        }
        return null;
    }
}
