package com.ruoyi.wxmini.controller;

import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.finance.domain.MerchantSettlementRecord;
import com.ruoyi.mall.finance.service.IMerchantSettlementRecordService;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.wxmini.dto.merchant.*;
import com.ruoyi.wxmini.service.IMerchantMiniService;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
    private IMerchantMiniService merchantMiniService;
    @Resource
    private IMerchantService merchantService;
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
        return AjaxResult.success(merchantMiniService.login(
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
        entryInfo.setMerchantImage(StringUtils.defaultIfBlank(merchant.getLogo(), merchant.getAvatar()));
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
        return AjaxResult.success(merchantMiniService.getWorkbenchOverview(WxMiniUserContext.getCurrentUserId()));
    }

    @GetMapping("/order/list")
    public AjaxResult listOrders(@RequestParam(required = false) String status) {
        AjaxResult accessDenied = checkAccess(PERMISSION_ORDER_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(merchantMiniService.listOrders(status));
    }

    @GetMapping("/order/detail/{orderNo}")
    public AjaxResult getOrderDetail(@PathVariable String orderNo) {
        AjaxResult accessDenied = checkAccess(PERMISSION_ORDER_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniService.getOrderDetail(orderNo));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @GetMapping("/booking/list")
    public AjaxResult listBookings(@RequestParam(required = false) String status) {
        AjaxResult accessDenied = checkAccess(PERMISSION_ORDER_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(merchantMiniService.listBookings(status));
    }

    @PostMapping("/booking/confirm/{bookingNo}")
    public AjaxResult confirmBooking(@PathVariable String bookingNo) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_ORDER_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniService.confirmBooking(bookingNo));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/booking/complete/{bookingNo}")
    public AjaxResult completeBooking(@PathVariable String bookingNo) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_ORDER_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniService.completeBooking(bookingNo));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/booking/cancel/{bookingNo}")
    public AjaxResult cancelBooking(@PathVariable String bookingNo) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_ORDER_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniService.cancelBooking(bookingNo));
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
            return AjaxResult.success("核销成功", merchantMiniService.writeOff(code, WxMiniUserContext.getCurrentUserId()));
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
            return AjaxResult.success(merchantMiniService.acceptOrder(orderNo));
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
            return AjaxResult.success(merchantMiniService.rejectOrder(orderNo, reason));
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
            return AjaxResult.success(merchantMiniService.cancelOrder(orderNo, reason));
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
            return AjaxResult.success(merchantMiniService.approveRefund(orderNo));
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
            return AjaxResult.success(merchantMiniService.rejectRefund(orderNo, reason));
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
        return AjaxResult.success(merchantMiniService.listVerifyRecords(status));
    }

    @GetMapping("/goods/list")
    public AjaxResult listGoods(@RequestParam(required = false) String status) {
        AjaxResult accessDenied = checkAccess(PERMISSION_GOODS_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(merchantMiniService.listGoods(status));
    }

    @GetMapping("/goods/detail/{id}")
    public AjaxResult getGoodsDetail(@PathVariable Long id) {
        AjaxResult accessDenied = checkAccess(PERMISSION_GOODS_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(merchantMiniService.getGoodsDetail(id));
    }

    @PostMapping("/goods/save")
    public AjaxResult saveGoods(@Valid @RequestBody MerchantMiniGoodsDto goodsDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_GOODS_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniService.saveGoods(goodsDto));
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
            return AjaxResult.success(merchantMiniService.updateGoodsStatus(goodsId, status));
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
            return AjaxResult.success(merchantMiniService.uploadGoodsImage(file));
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
            int count = merchantMiniService.batchUpdateGoodsStatus(goodsIds, status);
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
        return AjaxResult.success(merchantMiniService.getStoreProfile());
    }

    @PutMapping("/store/profile")
    public AjaxResult updateStoreProfile(@RequestBody(required = false) MerchantMiniStoreDto storeDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_STORE_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniService.updateStoreProfile(storeDto));
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
        return AjaxResult.success(merchantMiniService.listStaff());
    }

    @PutMapping("/staff/permission")
    public AjaxResult updateStaffPermission(@RequestBody(required = false) MerchantMiniStaffPermissionRequestDto requestDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_STAFF_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniService.updateStaffPermission(requestDto));
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
            return AjaxResult.success(merchantMiniService.addStaff(requestDto));
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
            return AjaxResult.success(merchantMiniService.updateStaff(requestDto));
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

    @GetMapping("/finance/daily-flow")
    public AjaxResult getDailyFlow(@RequestParam(required = false, defaultValue = "today") String range,
                                   @RequestParam(required = false) String date) {
        AjaxResult accessDenied = checkAccess(PERMISSION_FINANCE_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.success(buildDailyFlowOverview(range, date));
    }

    @PostMapping("/finance/withdraw")
    public AjaxResult applyWithdraw(@RequestBody(required = false) MerchantMiniWithdrawRequestDto requestDto) {
        AjaxResult accessDenied = checkStaffAccess(PERMISSION_FINANCE_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        return AjaxResult.error("当前使用微信支付商户号结算，无需平台提现");
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

    private MerchantMiniDailyFlowOverviewDto buildDailyFlowOverview(String range, String date) {
        Long merchantId = WxMiniUserContext.getCurrentMerchantId();
        DateRange dateRange = resolveDateRange(range, date);
        List<Map<String, Object>> rawDaily = settlementService.selectDailyFlowSummary(
                merchantId,
                dateRange.startDate.toString(),
                dateRange.endDate.toString()
        );
        List<MerchantSettlementRecord> rawRecords = settlementService.selectDailyFlowDetails(
                merchantId,
                dateRange.startDate.toString(),
                dateRange.endDate.toString(),
                80
        );

        Map<String, MerchantMiniDailyFlowDayDto> dailyMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rawDaily) {
            MerchantMiniDailyFlowDayDto dayDto = new MerchantMiniDailyFlowDayDto();
            dayDto.setDate(stringValue(row, "flowDate"));
            dayDto.setTotalAmount(centValue(row, "totalAmount"));
            dayDto.setMerchantAmount(centValue(row, "merchantAmount"));
            dayDto.setPlatformFeeAmount(centValue(row, "platformFeeAmount"));
            dayDto.setRefundAmount(centValue(row, "refundAmount"));
            dayDto.setOrderCount(intValue(row, "orderCount"));
            dailyMap.put(dayDto.getDate(), dayDto);
        }

        List<MerchantMiniDailyFlowDayDto> dailyList = new ArrayList<>();
        Long totalAmount = 0L;
        Long merchantAmount = 0L;
        Long platformFeeAmount = 0L;
        Long refundAmount = 0L;
        Integer orderCount = 0;
        for (LocalDate cursor = dateRange.endDate; !cursor.isBefore(dateRange.startDate); cursor = cursor.minusDays(1)) {
            MerchantMiniDailyFlowDayDto day = dailyMap.get(cursor.toString());
            if (day == null) {
                day = new MerchantMiniDailyFlowDayDto();
                day.setDate(cursor.toString());
                day.setTotalAmount(0L);
                day.setMerchantAmount(0L);
                day.setPlatformFeeAmount(0L);
                day.setRefundAmount(0L);
                day.setOrderCount(0);
            }
            totalAmount += safeLong(day.getTotalAmount());
            merchantAmount += safeLong(day.getMerchantAmount());
            platformFeeAmount += safeLong(day.getPlatformFeeAmount());
            refundAmount += safeLong(day.getRefundAmount());
            orderCount += day.getOrderCount() != null ? day.getOrderCount() : 0;
            dailyList.add(day);
        }

        List<MerchantMiniDailyFlowRecordDto> recordList = new ArrayList<>();
        for (MerchantSettlementRecord record : rawRecords) {
            MerchantMiniDailyFlowRecordDto recordDto = new MerchantMiniDailyFlowRecordDto();
            boolean refund = isRefundStatus(record.getStatus());
            recordDto.setId(record.getId());
            recordDto.setOrderNo(record.getOrderNo());
            recordDto.setTitle(record.getTitle());
            recordDto.setType(refund ? "refund" : "income");
            recordDto.setOrderAmount(yuanToAbsCent(record.getOrderAmount()));
            recordDto.setMerchantAmount(yuanToAbsCent(record.getMerchantAmount()));
            recordDto.setPlatformFeeAmount(yuanToAbsCent(record.getPlatformFeeAmount()));
            recordDto.setStatus(record.getStatus());
            recordDto.setFlowTime(record.getApplyTime() != null ? record.getApplyTime().getTime() : null);
            recordList.add(recordDto);
        }

        MerchantMiniDailyFlowOverviewDto dto = new MerchantMiniDailyFlowOverviewDto();
        dto.setRange(dateRange.range);
        dto.setStartDate(dateRange.startDate.toString());
        dto.setEndDate(dateRange.endDate.toString());
        dto.setTotalAmount(totalAmount);
        dto.setMerchantAmount(merchantAmount);
        dto.setPlatformFeeAmount(platformFeeAmount);
        dto.setRefundAmount(refundAmount);
        dto.setOrderCount(orderCount);
        dto.setDailyList(dailyList);
        dto.setRecordList(recordList);
        return dto;
    }

    private static Long yuanToCent(java.math.BigDecimal yuan) {
        if (yuan == null) return 0L;
        return yuan.movePointRight(2)
                .setScale(0, java.math.RoundingMode.UNNECESSARY)
                .longValueExact();
    }

    private static Long yuanToAbsCent(BigDecimal yuan) {
        if (yuan == null) return 0L;
        return yuan.abs()
                .movePointRight(2)
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValue();
    }

    private static DateRange resolveDateRange(String rawRange, String rawDate) {
        LocalDate today = LocalDate.now();
        String range = StringUtils.defaultIfBlank(rawRange, "today").toLowerCase(Locale.ROOT);
        if ("date".equals(range) && StringUtils.isNotBlank(rawDate)) {
            try {
                LocalDate selectedDate = LocalDate.parse(rawDate);
                return new DateRange("date", selectedDate, selectedDate);
            } catch (DateTimeParseException ignored) {
                return new DateRange("today", today, today);
            }
        }
        if ("yesterday".equals(range)) {
            LocalDate yesterday = today.minusDays(1);
            return new DateRange("yesterday", yesterday, yesterday);
        }
        if ("week".equals(range)) {
            return new DateRange("week", today.minusDays(6), today);
        }
        if ("month".equals(range)) {
            return new DateRange("month", today.withDayOfMonth(1), today);
        }
        return new DateRange("today", today, today);
    }

    private static String stringValue(Map<String, Object> row, String key) {
        Object value = valueOf(row, key);
        return value != null ? value.toString() : "";
    }

    private static Long centValue(Map<String, Object> row, String key) {
        Object value = valueOf(row, key);
        if (value instanceof BigDecimal) {
            return yuanToCent((BigDecimal) value);
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue())
                    .movePointRight(2)
                    .setScale(0, java.math.RoundingMode.HALF_UP)
                    .longValue();
        }
        return 0L;
    }

    private static Integer intValue(Map<String, Object> row, String key) {
        Object value = valueOf(row, key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    private static Object valueOf(Map<String, Object> row, String key) {
        if (row == null) return null;
        Object value = row.get(key);
        if (value != null) return value;
        value = row.get(key.toLowerCase(Locale.ROOT));
        if (value != null) return value;
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (key.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static Long safeLong(Long value) {
        return value != null ? value : 0L;
    }

    private static boolean isRefundStatus(String status) {
        return "CANCELLED".equals(status) || "REFUND_PROCESSING".equals(status) || "REVERSED".equals(status);
    }

    private static class DateRange {
        private final String range;
        private final LocalDate startDate;
        private final LocalDate endDate;

        private DateRange(String range, LocalDate startDate, LocalDate endDate) {
            this.range = range;
            this.startDate = startDate;
            this.endDate = endDate;
        }
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

    private AjaxResult checkAccess(String... permissionCodes) {
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
