package com.ruoyi.wxmini.controller;

import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.finance.domain.MerchantSettlementRecord;
import com.ruoyi.mall.finance.service.IMerchantSettlementRecordService;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.service.IWriteOffService;
import com.ruoyi.wxmini.dto.merchant.*;
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
import javax.validation.Valid;
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
    @Resource
    private IMerchantSettlementRecordService settlementService;

    /**
     * 每次请求前解析商家上下文：二维码入口优先，AppID模式仅作历史兼容。
     */
    @org.springframework.web.bind.annotation.ModelAttribute
    public void resolveMerchantContext(HttpServletRequest request) {
        resolveMerchantEntryId(request);
        resolveMerchantAppId(request);
    }

    private void resolveMerchantEntryId(HttpServletRequest request) {
        String merchantIdText = request.getHeader("X-Merchant-Id");
        if (StringUtils.isBlank(merchantIdText)) {
            merchantIdText = request.getParameter("merchantId");
        }
        if (StringUtils.isBlank(merchantIdText)) {
            return;
        }
        try {
            WxMiniUserContext.setMerchantEntryId(Long.valueOf(merchantIdText));
        } catch (NumberFormatException ignored) {
        }
    }

    private void resolveMerchantAppId(HttpServletRequest request) {
        String appId = request.getHeader("X-Merchant-AppId");
        if (StringUtils.isBlank(appId) || WxMiniUserContext.getAppIdMerchantId() != null) {
            return;
        }
        Merchant merchant = merchantService.selectMerchantByCAppId(appId);
        if (merchant == null) {
            merchant = merchantService.selectMerchantByMAppId(appId);
        }
        if (merchant != null) {
            WxMiniUserContext.setAppIdMerchantId(merchant.getId());
        }
    }

    @PostMapping("/auth/login")
    public AjaxResult login(@Valid @RequestBody MerchantMiniLoginRequestDto requestDto, HttpServletRequest request) {
        String appId = request.getHeader("X-Merchant-AppId");
        if (StringUtils.isBlank(appId)) {
            appId = request.getParameter("appid");
        }
        return AjaxResult.success(merchantMiniMockService.login(
                requestDto.getUsername(),
                requestDto.getPassword(),
                requestDto.getMerchantId(),
                appId
        ));
    }

    @GetMapping("/entry/{merchantId}")
    public AjaxResult getMerchantEntryInfo(@PathVariable Long merchantId) {
        Merchant merchant = merchantService.selectMerchantById(merchantId);
        if (merchant == null) {
            return AjaxResult.error("商家不存在");
        }
        if (merchant.getStatus() == Merchant.STATUS_STOPPED) {
            return AjaxResult.error("商家已停止合作");
        }
        if (merchant.getStatus() != Merchant.STATUS_NORMAL) {
            return AjaxResult.error("商家暂不可登录");
        }

        MerchantMiniEntryInfoDto entryInfo = new MerchantMiniEntryInfoDto();
        entryInfo.setMerchantId(merchant.getId());
        entryInfo.setMerchantName(merchant.getName());
        entryInfo.setContact(merchant.getContact());
        entryInfo.setPhone(merchant.getPhone());
        entryInfo.setLoginPage("/pages/merchant/login/login?merchantId=" + merchant.getId());
        entryInfo.setEntryAppId(merchant.getCMiniAppId());
        entryInfo.setMiniAppConfigured(StringUtils.isNotBlank(merchant.getCMiniAppId()));
        return AjaxResult.success(entryInfo);
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

    @GetMapping("/goods/detail/{id}")
    public AjaxResult getGoodsDetail(@PathVariable Long id) {
        AjaxResult accessDenied = checkAccess(PERMISSION_GOODS_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(merchantMiniMockService.getGoodsDetail(id));
    }

    @PostMapping("/goods/save")
    public AjaxResult saveGoods(@Valid @RequestBody MerchantMiniGoodsDto goodsDto) {
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
    public AjaxResult addStaff(@Valid @RequestBody MerchantMiniStaffRequestDto requestDto) {
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
    public AjaxResult updateStaff(@Valid @RequestBody MerchantMiniStaffRequestDto requestDto) {
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
        return AjaxResult.success(buildSettlementOverview());
    }

    @GetMapping("/settlement/overview")
    public AjaxResult getSettlementOverview() {
        AjaxResult accessDenied = checkAccess(PERMISSION_FINANCE_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(buildSettlementOverview());
    }

    @PostMapping("/finance/withdraw")
    public AjaxResult applyWithdraw(@RequestBody(required = false) MerchantMiniWithdrawRequestDto requestDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_FINANCE_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.error("该版本已切换为微信自动结算，无需商家手动提现");
    }

    // ==================== 结算概览（真实数据） ====================

    private MerchantMiniSettlementOverviewDto buildSettlementOverview() {
        Long merchantId = WxMiniUserContext.getCurrentMerchantId();
        MerchantMiniSettlementOverviewDto dto = new MerchantMiniSettlementOverviewDto();

        // 金额转分
        java.math.BigDecimal todayIncome = settlementService.sumMerchantAmountToday(merchantId);
        java.math.BigDecimal monthIncome = settlementService.sumMerchantAmountThisMonth(merchantId);
        java.math.BigDecimal pendingAmount = settlementService.sumMerchantAmountByStatus(merchantId, "WAITING_T1");
        java.math.BigDecimal settledAmount = settlementService.sumMerchantAmountByStatus(merchantId, "ARRIVED");
        java.math.BigDecimal processingAmount = settlementService.sumMerchantAmountByStatus(merchantId, "TRANSFERRING");
        Integer completedCount = settlementService.countCompletedByMerchantId(merchantId);

        dto.setTodayIncomeAmount(yuanToCent(todayIncome));
        dto.setMonthIncomeAmount(yuanToCent(monthIncome));
        dto.setPendingSettleAmount(yuanToCent(pendingAmount));
        dto.setSettledAmount(yuanToCent(settledAmount));
        dto.setProcessingAmount(yuanToCent(processingAmount));
        dto.setPendingAutoTransferAmount(yuanToCent(pendingAmount));
        dto.setPlatformFeeAmount(0L);
        dto.setCompletedOrderCount(completedCount != null ? completedCount : 0);
        dto.setAutoTransferMode("T+1");
        dto.setNextAutoTransferTime(nextAutoTransferTimeMillis());

        // 结算账号（待配置）
        MerchantMiniSettlementAccountDto account = new MerchantMiniSettlementAccountDto();
        account.setStatus("PENDING");
        dto.setSettlementAccount(account);

        // 结算记录列表（最近50条）
        MerchantSettlementRecord query = new MerchantSettlementRecord();
        query.setMerchantId(merchantId);
        List<MerchantSettlementRecord> records = settlementService.selectList(query);
        List<MerchantMiniSettlementRecordDto> recordDtos = new ArrayList<>();
        int count = 0;
        for (MerchantSettlementRecord r : records) {
            if (count++ >= 50) break;
            MerchantMiniSettlementRecordDto rd = new MerchantMiniSettlementRecordDto();
            rd.setSettlementId(r.getSettlementNo());
            rd.setOrderNo(r.getOrderNo());
            rd.setTitle(r.getTitle());
            rd.setAmount(yuanToCent(r.getMerchantAmount()));
            rd.setStatus(r.getStatus());
            rd.setApplyTime(r.getApplyTime() != null ? r.getApplyTime().getTime() : null);
            rd.setExpectedTransferTime(r.getExpectedTransferTime() != null ? r.getExpectedTransferTime().getTime() : null);
            rd.setArriveTime(r.getArriveTime() != null ? r.getArriveTime().getTime() : null);
            rd.setRemark(r.getFailReason());
            recordDtos.add(rd);
        }
        dto.setSettlementRecordList(recordDtos);

        // 流水列表（从记录构建）
        List<MerchantMiniFinanceLedgerDto> ledgerList = new ArrayList<>();
        for (MerchantSettlementRecord r : records) {
            if (ledgerList.size() >= 50) break;
            if ("CANCELLED".equals(r.getStatus()) || "REVERSED".equals(r.getStatus())) continue;
            MerchantMiniFinanceLedgerDto ld = new MerchantMiniFinanceLedgerDto();
            ld.setLedgerId(r.getId());
            ld.setOrderNo(r.getOrderNo());
            ld.setTitle(r.getTitle());
            ld.setOrderAmount(yuanToCent(r.getOrderAmount()));
            ld.setMerchantAmount(yuanToCent(r.getMerchantAmount()));
            ld.setPlatformFeeAmount(yuanToCent(r.getPlatformFeeAmount()));
            ld.setStatus("ARRIVED".equals(r.getStatus()) || "TRANSFERRING".equals(r.getStatus()) ? "SETTLED" : "PENDING");
            ld.setFinishTime(r.getApplyTime() != null ? r.getApplyTime().getTime() : null);
            ld.setSettleTime(r.getExpectedTransferTime() != null ? r.getExpectedTransferTime().getTime() : null);
            ledgerList.add(ld);
        }
        dto.setLedgerList(ledgerList);

        return dto;
    }

    private static Long yuanToCent(java.math.BigDecimal yuan) {
        if (yuan == null) return 0L;
        return yuan.movePointRight(2)
                .setScale(0, java.math.RoundingMode.UNNECESSARY)
                .longValueExact();
    }

    private static Long nextAutoTransferTimeMillis() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 10);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
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
