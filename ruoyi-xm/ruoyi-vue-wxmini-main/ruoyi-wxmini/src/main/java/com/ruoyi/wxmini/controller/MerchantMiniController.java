package com.ruoyi.wxmini.controller;

import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.service.IWriteOffService;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniGoodsDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniGoodsStatusRequestDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniLoginRequestDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniReasonRequestDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniStaffPermissionRequestDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniStaffRequestDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniStoreDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniWithdrawRequestDto;
import com.ruoyi.wxmini.service.IMerchantMiniMockService;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wxmini/merchant-mini")
public class MerchantMiniController {

    private static final String PERMISSION_STATS_VIEW = "stats.view";
    private static final String PERMISSION_ORDER_MANAGE = "order.manage";
    private static final String PERMISSION_VERIFY_SCAN = "verify.scan";
    private static final String PERMISSION_VERIFY_MANUAL = "verify.manual";
    private static final String PERMISSION_GOODS_MANAGE = "goods.manage";
    private static final String PERMISSION_STORE_MANAGE = "store.manage";
    private static final String PERMISSION_STAFF_MANAGE = "staff.manage";
    private static final String PERMISSION_VERIFY_RECORD = "verify.record";
    private static final String PERMISSION_FINANCE_MANAGE = "finance.manage";

    @Resource
    private IMerchantMiniMockService merchantMiniMockService;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private IWriteOffService writeOffService;

    /**
     * 每次请求前，从AppID解析商家ID并设置上下文
     */
    @org.springframework.web.bind.annotation.ModelAttribute
    public void resolveMerchantFromAppId(HttpServletRequest request) {
        String appId = request.getHeader("X-Merchant-AppId");
        if (StringUtils.isNotBlank(appId) && WxMiniUserContext.getAppIdMerchantId() == null) {
            Merchant merchant = merchantService.selectMerchantByMAppId(appId);
            if (merchant != null) {
                WxMiniUserContext.setAppIdMerchantId(merchant.getId());
            }
        }
    }

    @PostMapping("/auth/login")
    public AjaxResult login(@RequestBody(required = false) MerchantMiniLoginRequestDto requestDto) {
        String username = requestDto == null ? null : requestDto.getUsername();
        String password = requestDto == null ? null : requestDto.getPassword();
        return AjaxResult.success(merchantMiniMockService.login(username, password));
    }

    @GetMapping("/workbench/overview")
    public AjaxResult getWorkbenchOverview() {
        AjaxResult accessDenied = checkAccess(PERMISSION_STATS_VIEW);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(merchantMiniMockService.getWorkbenchOverview(WxMiniUserContext.getCurrentUserId()));
    }

    @GetMapping("/order/list")
    public AjaxResult listOrders(@RequestParam(required = false) String status) {
        AjaxResult accessDenied = checkAccess(PERMISSION_ORDER_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(merchantMiniMockService.listOrders(status));
    }

    @GetMapping("/order/detail/{orderNo}")
    public AjaxResult getOrderDetail(@PathVariable String orderNo) {
        AjaxResult accessDenied = checkAccess(PERMISSION_ORDER_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniMockService.getOrderDetail(orderNo));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/order/write-off/{code}")
    public AjaxResult writeOff(@PathVariable String code) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_VERIFY_SCAN, PERMISSION_VERIFY_MANUAL);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            Long merchantId = WxMiniUserContext.getCurrentMerchantId();
            Long operatorId = WxMiniUserContext.getCurrentStaffId();
            return AjaxResult.success("核销成功", writeOffService.writeOff(code, merchantId, operatorId, ""));
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/order/accept/{orderNo}")
    public AjaxResult acceptOrder(@PathVariable String orderNo) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_ORDER_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniMockService.acceptOrder(orderNo));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/order/reject/{orderNo}")
    public AjaxResult rejectOrder(@PathVariable String orderNo, @RequestBody(required = false) MerchantMiniReasonRequestDto requestDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_ORDER_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            String reason = requestDto != null ? requestDto.getReason() : null;
            return AjaxResult.success(merchantMiniMockService.rejectOrder(orderNo, reason));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/order/cancel/{orderNo}")
    public AjaxResult cancelOrder(@PathVariable String orderNo, @RequestBody(required = false) MerchantMiniReasonRequestDto requestDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_ORDER_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            String reason = requestDto != null ? requestDto.getReason() : null;
            return AjaxResult.success(merchantMiniMockService.cancelOrder(orderNo, reason));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/order/refund/approve/{orderNo}")
    public AjaxResult approveRefund(@PathVariable String orderNo) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_ORDER_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniMockService.approveRefund(orderNo));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/order/refund/reject/{orderNo}")
    public AjaxResult rejectRefund(@PathVariable String orderNo, @RequestBody(required = false) MerchantMiniReasonRequestDto requestDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_ORDER_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            String reason = requestDto != null ? requestDto.getReason() : null;
            return AjaxResult.success(merchantMiniMockService.rejectRefund(orderNo, reason));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @GetMapping("/verify/record/list")
    public AjaxResult listVerifyRecords(@RequestParam(required = false) String status) {
        AjaxResult accessDenied = checkAccess(PERMISSION_VERIFY_RECORD, PERMISSION_VERIFY_SCAN, PERMISSION_VERIFY_MANUAL);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(merchantMiniMockService.listVerifyRecords(status));
    }

    @GetMapping("/goods/list")
    public AjaxResult listGoods(@RequestParam(required = false) String status) {
        AjaxResult accessDenied = checkAccess(PERMISSION_GOODS_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(merchantMiniMockService.listGoods(status));
    }

    @PostMapping("/goods/save")
    public AjaxResult saveGoods(@RequestBody(required = false) MerchantMiniGoodsDto goodsDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_GOODS_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniMockService.saveGoods(goodsDto));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PutMapping("/goods/status")
    public AjaxResult updateGoodsStatus(@RequestBody(required = false) MerchantMiniGoodsStatusRequestDto requestDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_GOODS_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            Long goodsId = requestDto == null ? null : requestDto.getGoodsId();
            String status = requestDto == null ? null : requestDto.getStatus();
            return AjaxResult.success(merchantMiniMockService.updateGoodsStatus(goodsId, status));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/goods/image/upload")
    public AjaxResult uploadGoodsImage(@RequestParam(value = "file", required = false) MultipartFile file) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_GOODS_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniMockService.uploadGoodsImage(file));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PutMapping("/goods/batch-status")
    public AjaxResult batchUpdateGoodsStatus(@RequestBody Map<String, Object> params) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_GOODS_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            @SuppressWarnings("unchecked")
            List<Number> ids = (List<Number>) params.get("goodsIds");
            String status = (String) params.get("status");
            List<Long> goodsIds = new ArrayList<>();
            if (ids != null) {
                for (Number n : ids) {
                    goodsIds.add(n.longValue());
                }
            }
            int count = merchantMiniMockService.batchUpdateGoodsStatus(goodsIds, status);
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return AjaxResult.success(result);
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @GetMapping("/store/profile")
    public AjaxResult getStoreProfile() {
        AjaxResult accessDenied = checkAccess(PERMISSION_STORE_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(merchantMiniMockService.getStoreProfile());
    }

    @PutMapping("/store/profile")
    public AjaxResult updateStoreProfile(@RequestBody(required = false) MerchantMiniStoreDto storeDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_STORE_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniMockService.updateStoreProfile(storeDto));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @GetMapping("/staff/list")
    public AjaxResult listStaff() {
        AjaxResult accessDenied = checkAccess(PERMISSION_STAFF_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(merchantMiniMockService.listStaff());
    }

    @PutMapping("/staff/permission")
    public AjaxResult updateStaffPermission(@RequestBody(required = false) MerchantMiniStaffPermissionRequestDto requestDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_STAFF_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniMockService.updateStaffPermission(requestDto));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/staff/add")
    public AjaxResult addStaff(@RequestBody(required = false) MerchantMiniStaffRequestDto requestDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_STAFF_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniMockService.addStaff(requestDto));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PutMapping("/staff/update")
    public AjaxResult updateStaff(@RequestBody(required = false) MerchantMiniStaffRequestDto requestDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_STAFF_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniMockService.updateStaff(requestDto));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @GetMapping("/finance/overview")
    public AjaxResult getFinanceOverview() {
        AjaxResult accessDenied = checkAccess(PERMISSION_FINANCE_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(merchantMiniMockService.getFinanceOverview());
    }

    @PostMapping("/finance/withdraw")
    public AjaxResult applyWithdraw(@RequestBody(required = false) MerchantMiniWithdrawRequestDto requestDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_FINANCE_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            Long amount = requestDto == null ? null : requestDto.getAmount();
            return AjaxResult.success(merchantMiniMockService.applyWithdraw(amount));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    // ==================== 营销模块（Stub，待数据库表就绪后实现） ====================

    private static final String PERMISSION_MARKETING_MANAGE = "marketing.manage";

    @GetMapping("/marketing/coupon/list")
    public AjaxResult listCoupons() {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_MARKETING_MANAGE);
        if (accessDenied != null) return accessDenied;
        // TODO: coupon表未建，暂返回空列表
        return AjaxResult.success(new ArrayList<>());
    }

    @PostMapping("/marketing/coupon/save")
    public AjaxResult saveCoupon(@RequestBody(required = false) Map<String, Object> params) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_MARKETING_MANAGE);
        if (accessDenied != null) return accessDenied;
        // TODO: coupon表未建，暂返回成功
        Map<String, Object> result = new HashMap<>();
        result.put("id", 0);
        return AjaxResult.success(result);
    }

    @PutMapping("/marketing/coupon/status")
    public AjaxResult updateCouponStatus(@RequestBody(required = false) Map<String, Object> params) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_MARKETING_MANAGE);
        if (accessDenied != null) return accessDenied;
        // TODO: coupon表未建，暂返回成功
        return AjaxResult.success("ok");
    }

    @GetMapping("/marketing/promotion/list")
    public AjaxResult listPromotions() {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_MARKETING_MANAGE);
        if (accessDenied != null) return accessDenied;
        // TODO: promotion表未建，暂返回空列表
        return AjaxResult.success(new ArrayList<>());
    }

    @PostMapping("/marketing/promotion/save")
    public AjaxResult savePromotion(@RequestBody(required = false) Map<String, Object> params) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_MARKETING_MANAGE);
        if (accessDenied != null) return accessDenied;
        // TODO: promotion表未建，暂返回成功
        Map<String, Object> result = new HashMap<>();
        result.put("id", 0);
        return AjaxResult.success(result);
    }

    // ==================== 入驻申请（Stub，待数据库表就绪后实现） ====================

    @PostMapping("/apply/submit")
    public AjaxResult submitApply(@RequestBody(required = false) Map<String, Object> params) {
        // TODO: 入驻申请表未建，暂返回成功
        Map<String, Object> result = new HashMap<>();
        result.put("applyNo", "APPLY" + System.currentTimeMillis());
        result.put("status", "PENDING");
        return AjaxResult.success(result);
    }

    @GetMapping("/apply/status")
    public AjaxResult getApplyStatus() {
        // TODO: 入驻申请表未建，暂返回空
        return AjaxResult.success(null);
    }

    private AjaxResult checkAccess(String... permissionCodes) {
        // 通过AppID识别商家的场景：仅允许数据浏览
        if (WxMiniUserContext.getAppIdMerchantId() != null && !WxMiniUserContext.isMerchantStaff()) {
            return null; // AppID模式下允许访问（数据按AppID隔离）
        }
        if (!WxMiniUserContext.isMerchantStaff()) {
            return AjaxResult.error(HttpStatus.FORBIDDEN, "仅商家员工可访问");
        }
        if (!WxMiniUserContext.hasAnyPermission(permissionCodes)) {
            return AjaxResult.error(HttpStatus.FORBIDDEN, "无权限");
        }
        return null;
    }

    /**
     * 操作类接口检查：必须有登录token
     */
    private AjaxResult checkStaffAccess(String... permissionCodes) {
        if (!WxMiniUserContext.isMerchantStaff()) {
            return AjaxResult.error(HttpStatus.FORBIDDEN, "请先登录");
        }
        if (!WxMiniUserContext.hasAnyPermission(permissionCodes)) {
            return AjaxResult.error(HttpStatus.FORBIDDEN, "无权限");
        }
        return null;
    }
}
