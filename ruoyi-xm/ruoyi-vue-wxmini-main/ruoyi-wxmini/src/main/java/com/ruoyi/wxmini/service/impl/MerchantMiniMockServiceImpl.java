package com.ruoyi.wxmini.service.impl;

import com.ruoyi.mall.common.bo.WxMiniAuthContext;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniFinanceLedgerDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniFinanceOverviewDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniGoodsDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniLoginResponseDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniOrderDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniOverviewDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniSettlementAccountDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniSettlementOverviewDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniSettlementRecordDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniStaffPermissionRequestDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniStaffRequestDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniStaffUserDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniStoreDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniUploadResultDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniVerifyRecordDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniWithdrawRecordDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniWorkbenchStatsDto;
import com.ruoyi.wxmini.service.IMerchantMiniMockService;
import com.ruoyi.mall.common.service.IWxMiniJwtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("dev")  // 仅开发环境生效，生产环境不注册此Bean
public class MerchantMiniMockServiceImpl implements IMerchantMiniMockService {

    private static final Long MERCHANT_ID = 1L;
    private static final Long STORE_ID = 1001L;

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String GOODS_STATUS_ON_SHELF = "ON_SHELF";
    private static final String GOODS_STATUS_OFF_SHELF = "OFF_SHELF";
    private static final String STATUS_PENDING_VERIFY = "PENDING_VERIFY";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_REFUNDING = "REFUNDING";
    private static final String VERIFY_STATUS_SUCCESS = "SUCCESS";
    private static final String VERIFY_STATUS_FAILED = "FAILED";
    private static final String LEDGER_STATUS_PENDING = "PENDING";
    private static final String LEDGER_STATUS_SETTLED = "SETTLED";
    private static final String SETTLEMENT_STATUS_WAITING_T1 = "WAITING_T1";
    private static final String SETTLEMENT_STATUS_ARRIVED = "ARRIVED";
    private static final String WITHDRAW_STATUS_PROCESSING = "PROCESSING";
    private static final String ROLE_OWNER = "owner";
    private static final int MERCHANT_RATE = 90;
    private static final int PLATFORM_RATE = 10;
    private static final long DAY_MILLIS = 24L * 60L * 60L * 1000L;

    @Resource
    private IWxMiniJwtService jwtService;

    private final Map<String, MerchantMiniStaffUserDto> roleStaffMap = initRoleStaffMap();

    private final MerchantMiniStoreDto storeInfo = initStoreInfo();

    private final List<MerchantMiniGoodsDto> goodsList = initGoodsList();

    private final List<MerchantMiniOrderDto> orderList = initOrderList();

    private final List<MerchantMiniVerifyRecordDto> verifyRecordList = initVerifyRecordList();

    private final List<MerchantMiniWithdrawRecordDto> withdrawRecordList = initWithdrawRecordList();

    @Override
    public MerchantMiniLoginResponseDto login(String username, String password, String appid) {
        // Mock模式：忽略密码，用username当作roleKey
        MerchantMiniStaffUserDto staffUser = cloneStaffUser(resolveStaffUserByRoleKey(username));
        MerchantMiniLoginResponseDto responseDto = new MerchantMiniLoginResponseDto();
        responseDto.setToken(jwtService.createToken(buildAuthContext(staffUser)));
        responseDto.setStaffUser(staffUser);
        return responseDto;
    }

    @Override
    public MerchantMiniOverviewDto getWorkbenchOverview(String currentUserId) {
        MerchantMiniOverviewDto overviewDto = new MerchantMiniOverviewDto();
        overviewDto.setStaffUser(cloneStaffUser(resolveStaffUserByUserId(currentUserId)));
        overviewDto.setStoreInfo(cloneStoreInfo(this.storeInfo));
        overviewDto.setStats(buildStats());
        overviewDto.setPendingOrderList(buildPendingOrderList());
        return overviewDto;
    }

    @Override
    public synchronized List<MerchantMiniOrderDto> listOrders(String status) {
        List<MerchantMiniOrderDto> result = new ArrayList<>();
        for (MerchantMiniOrderDto orderDto : orderList) {
            if (StringUtils.isNotBlank(status) && !status.equals(orderDto.getStatus())) {
                continue;
            }
            result.add(cloneOrder(orderDto));
        }
        return result;
    }

    @Override
    public synchronized MerchantMiniOrderDto getOrderDetail(String orderNo) {
        MerchantMiniOrderDto orderDto = findOrder(orderNo);
        if (orderDto == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return cloneOrder(orderDto);
    }

    @Override
    public synchronized MerchantMiniOrderDto writeOff(String code, String currentUserId) {
        MerchantMiniStaffUserDto staffUser = resolveStaffUserByUserId(currentUserId);
        MerchantMiniOrderDto targetOrder = findOrderByVerifyCode(code);

        if (targetOrder == null) {
            addVerifyRecord(null, code, staffUser, VERIFY_STATUS_FAILED, "未找到对应订单");
            throw new IllegalArgumentException("未找到对应订单");
        }
        if (STATUS_COMPLETED.equals(targetOrder.getStatus())) {
            addVerifyRecord(targetOrder, code, staffUser, VERIFY_STATUS_FAILED, "该订单已核销完成");
            throw new IllegalArgumentException("该订单已核销完成");
        }
        if (!STATUS_PENDING_VERIFY.equals(targetOrder.getStatus())) {
            addVerifyRecord(targetOrder, code, staffUser, VERIFY_STATUS_FAILED, "当前订单状态不可核销");
            throw new IllegalArgumentException("当前订单状态不可核销");
        }
        if (isGoodsExpired(targetOrder)) {
            addVerifyRecord(targetOrder, code, staffUser, VERIFY_STATUS_FAILED, "团购券已过有效期");
            throw new IllegalArgumentException("团购券已过有效期");
        }

        targetOrder.setStatus(STATUS_COMPLETED);
        targetOrder.setVerifyTime(System.currentTimeMillis());
        targetOrder.setVerifyStaffName(staffUser.getName());
        addVerifyRecord(targetOrder, code, staffUser, VERIFY_STATUS_SUCCESS, null);
        return cloneOrder(targetOrder);
    }

    @Override
    public synchronized List<MerchantMiniVerifyRecordDto> listVerifyRecords(String status) {
        List<MerchantMiniVerifyRecordDto> result = new ArrayList<>();
        for (MerchantMiniVerifyRecordDto recordDto : verifyRecordList) {
            if (StringUtils.isNotBlank(status) && !"ALL".equals(status) && !status.equals(recordDto.getStatus())) {
                continue;
            }
            result.add(cloneVerifyRecord(recordDto));
        }
        result.sort((a, b) -> Long.compare(defaultLong(b.getVerifyTime()), defaultLong(a.getVerifyTime())));
        return result;
    }

    @Override
    public synchronized List<MerchantMiniGoodsDto> listGoods(String status) {
        List<MerchantMiniGoodsDto> result = new ArrayList<>();
        for (MerchantMiniGoodsDto goodsDto : goodsList) {
            if (StringUtils.isNotBlank(status) && !"ALL".equals(status) && !status.equals(goodsDto.getStatus())) {
                continue;
            }
            result.add(cloneGoods(goodsDto));
        }
        result.sort((a, b) -> Integer.compare(defaultInt(a.getSort()), defaultInt(b.getSort())));
        return result;
    }

    @Override
    public synchronized MerchantMiniGoodsDto getGoodsDetail(Long id) {
        for (MerchantMiniGoodsDto goodsDto : goodsList) {
            if (goodsDto.getGoodsId() != null && goodsDto.getGoodsId().equals(id)) {
                return cloneGoods(goodsDto);
            }
        }
        throw new IllegalArgumentException("商品不存在");
    }

    @Override
    public synchronized MerchantMiniGoodsDto saveGoods(MerchantMiniGoodsDto goodsDto) {
        if (goodsDto == null || StringUtils.isBlank(goodsDto.getTitle())) {
            throw new IllegalArgumentException("套餐名称不能为空");
        }
        MerchantMiniGoodsDto nextGoods = cloneGoods(goodsDto);
        if (nextGoods.getGoodsId() == null) {
            nextGoods.setGoodsId(nextGoodsId());
            nextGoods.setSales(0);
            nextGoods.setSort(goodsList.size() + 1);
        }
        if (nextGoods.getStatus() == null) {
            nextGoods.setStatus(GOODS_STATUS_ON_SHELF);
        }
        if (nextGoods.getSales() == null) {
            nextGoods.setSales(0);
        }
        if (nextGoods.getSort() == null) {
            nextGoods.setSort(goodsList.size() + 1);
        }

        boolean updated = false;
        for (int i = 0; i < goodsList.size(); i++) {
            if (goodsList.get(i).getGoodsId().equals(nextGoods.getGoodsId())) {
                goodsList.set(i, nextGoods);
                updated = true;
                break;
            }
        }
        if (!updated) {
            goodsList.add(nextGoods);
        }
        return cloneGoods(nextGoods);
    }

    @Override
    public synchronized MerchantMiniGoodsDto updateGoodsStatus(Long goodsId, String status) {
        if (goodsId == null) {
            throw new IllegalArgumentException("套餐ID不能为空");
        }
        if (!GOODS_STATUS_ON_SHELF.equals(status) && !GOODS_STATUS_OFF_SHELF.equals(status)) {
            throw new IllegalArgumentException("套餐状态不合法");
        }
        MerchantMiniGoodsDto targetGoods = findGoods(goodsId);
        if (targetGoods == null) {
            throw new IllegalArgumentException("套餐不存在");
        }
        targetGoods.setStatus(status);
        return cloneGoods(targetGoods);
    }

    @Override
    public synchronized MerchantMiniUploadResultDto uploadGoodsImage(String fileName, Long size) {
        MerchantMiniUploadResultDto resultDto = new MerchantMiniUploadResultDto();
        String safeFileName = StringUtils.isBlank(fileName) ? "goods-image.jpg" : fileName.replace("\\", "_").replace("/", "_");
        resultDto.setFileName(safeFileName);
        resultDto.setSize(size == null ? 0L : size);
        resultDto.setUrl("/profile/merchant-goods/" + System.currentTimeMillis() + "_" + safeFileName);
        return resultDto;
    }

    @Override
    public MerchantMiniUploadResultDto uploadGoodsImage(org.springframework.web.multipart.MultipartFile file) {
        return uploadGoodsImage(file != null ? file.getOriginalFilename() : null, file != null ? file.getSize() : 0L);
    }

    @Override
    public int batchUpdateGoodsStatus(List<Long> goodsIds, String status) {
        int count = 0;
        for (Long goodsId : goodsIds) {
            try {
                updateGoodsStatus(goodsId, status);
                count++;
            } catch (Exception ignored) {}
        }
        return count;
    }

    @Override
    public synchronized MerchantMiniStoreDto getStoreProfile() {
        return cloneStoreInfo(storeInfo);
    }

    @Override
    public synchronized MerchantMiniStoreDto updateStoreProfile(MerchantMiniStoreDto storeDto) {
        if (storeDto == null) {
            throw new IllegalArgumentException("门店信息不能为空");
        }
        storeInfo.setStoreName(storeDto.getStoreName());
        storeInfo.setBrandSlogan(storeDto.getBrandSlogan());
        storeInfo.setNotice(storeDto.getNotice());
        storeInfo.setBusinessHours(storeDto.getBusinessHours());
        storeInfo.setPhone(storeDto.getPhone());
        storeInfo.setAddress(storeDto.getAddress());
        storeInfo.setServiceTags(new ArrayList<>(safeList(storeDto.getServiceTags())));
        storeInfo.setBannerTitles(new ArrayList<>(safeList(storeDto.getBannerTitles())));
        storeInfo.setBusinessStatus(storeDto.getBusinessStatus());
        storeInfo.setSupportRefund(storeDto.getSupportRefund());
        storeInfo.setSupportBooking(storeDto.getSupportBooking());
        return cloneStoreInfo(storeInfo);
    }

    @Override
    public synchronized List<MerchantMiniStaffUserDto> listStaff() {
        return cloneStaffList();
    }

    @Override
    public synchronized List<MerchantMiniStaffUserDto> updateStaffPermission(MerchantMiniStaffPermissionRequestDto requestDto) {
        if (requestDto == null || requestDto.getStaffId() == null) {
            throw new IllegalArgumentException("员工ID不能为空");
        }
        MerchantMiniStaffUserDto targetStaff = resolveStaffUserByStaffId(requestDto.getStaffId());
        if (targetStaff == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        if (requestDto.getPermissions() != null) {
            targetStaff.setPermissions(new ArrayList<>(requestDto.getPermissions()));
        }
        if (StringUtils.isNotBlank(requestDto.getStatus())) {
            targetStaff.setStatus(requestDto.getStatus());
        }
        return cloneStaffList();
    }

    @Override
    public synchronized MerchantMiniFinanceOverviewDto getFinanceOverview() {
        MerchantMiniSettlementOverviewDto settlementOverview = getSettlementOverview();
        MerchantMiniFinanceOverviewDto overviewDto = new MerchantMiniFinanceOverviewDto();
        overviewDto.setTodayIncomeAmount(settlementOverview.getTodayIncomeAmount());
        overviewDto.setMonthIncomeAmount(settlementOverview.getMonthIncomeAmount());
        overviewDto.setPendingSettleAmount(settlementOverview.getPendingSettleAmount());
        overviewDto.setWithdrawableAmount(settlementOverview.getSettledAmount());
        overviewDto.setPlatformFeeAmount(settlementOverview.getPlatformFeeAmount());
        overviewDto.setCompletedOrderCount(settlementOverview.getCompletedOrderCount());
        overviewDto.setLedgerList(settlementOverview.getLedgerList());
        overviewDto.setWithdrawList(new ArrayList<>());
        return overviewDto;
    }

    @Override
    public synchronized MerchantMiniSettlementOverviewDto getSettlementOverview() {
        List<MerchantMiniFinanceLedgerDto> ledgerList = buildFinanceLedgerList();
        Long todayIncomeAmount = 0L;
        Long monthIncomeAmount = 0L;
        Long pendingSettleAmount = 0L;
        Long settledAmount = 0L;
        Long platformFeeAmount = 0L;
        int completedOrderCount = 0;
        List<MerchantMiniSettlementRecordDto> settlementRecordList = new ArrayList<>();

        for (MerchantMiniFinanceLedgerDto ledgerDto : ledgerList) {
            completedOrderCount++;
            platformFeeAmount += defaultLong(ledgerDto.getPlatformFeeAmount());
            if (isToday(ledgerDto.getFinishTime())) {
                todayIncomeAmount += defaultLong(ledgerDto.getMerchantAmount());
            }
            if (isCurrentMonth(ledgerDto.getFinishTime())) {
                monthIncomeAmount += defaultLong(ledgerDto.getMerchantAmount());
            }
            if (LEDGER_STATUS_PENDING.equals(ledgerDto.getStatus())) {
                pendingSettleAmount += defaultLong(ledgerDto.getMerchantAmount());
            } else {
                settledAmount += defaultLong(ledgerDto.getMerchantAmount());
            }
            settlementRecordList.add(buildSettlementRecord(ledgerDto));
        }

        MerchantMiniSettlementOverviewDto overviewDto = new MerchantMiniSettlementOverviewDto();
        overviewDto.setTodayIncomeAmount(todayIncomeAmount);
        overviewDto.setMonthIncomeAmount(monthIncomeAmount);
        overviewDto.setPendingSettleAmount(pendingSettleAmount);
        overviewDto.setSettledAmount(settledAmount);
        overviewDto.setProcessingAmount(0L);
        overviewDto.setPendingAutoTransferAmount(pendingSettleAmount);
        overviewDto.setPlatformFeeAmount(platformFeeAmount);
        overviewDto.setCompletedOrderCount(completedOrderCount);
        overviewDto.setAutoTransferMode("T+1");
        overviewDto.setNextAutoTransferTime(nextAutoTransferTime());
        overviewDto.setSettlementAccount(buildSettlementAccount());
        overviewDto.setSettlementRecordList(settlementRecordList);
        overviewDto.setLedgerList(ledgerList);
        return overviewDto;
    }

    @Override
    public synchronized MerchantMiniWithdrawRecordDto applyWithdraw(Long amount) {
        throw new IllegalArgumentException("该版本已切换为微信自动结算，无需商家手动提现");
    }

    // ==================== 订单操作 ====================

    @Override
    public synchronized MerchantMiniOrderDto acceptOrder(String orderNo) {
        MerchantMiniOrderDto order = findOrder(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!STATUS_PENDING_VERIFY.equals(order.getStatus()) && !"PENDING_ACCEPT".equals(order.getStatus())) {
            throw new IllegalArgumentException("当前订单状态不可接单");
        }
        order.setStatus(STATUS_PENDING_VERIFY);
        return cloneOrder(order);
    }

    @Override
    public synchronized MerchantMiniOrderDto rejectOrder(String orderNo, String reason) {
        MerchantMiniOrderDto order = findOrder(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!STATUS_PENDING_VERIFY.equals(order.getStatus()) && !"PENDING_ACCEPT".equals(order.getStatus())) {
            throw new IllegalArgumentException("当前订单状态不可拒单");
        }
        order.setStatus("CANCELLED");
        return cloneOrder(order);
    }

    @Override
    public synchronized MerchantMiniOrderDto cancelOrder(String orderNo, String reason) {
        MerchantMiniOrderDto order = findOrder(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (STATUS_COMPLETED.equals(order.getStatus())) {
            throw new IllegalArgumentException("已完成订单不可取消");
        }
        order.setStatus("CANCELLED");
        return cloneOrder(order);
    }

    @Override
    public synchronized MerchantMiniOrderDto approveRefund(String orderNo) {
        MerchantMiniOrderDto order = findOrder(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!STATUS_REFUNDING.equals(order.getStatus())) {
            throw new IllegalArgumentException("当前订单状态不可同意退款");
        }
        order.setStatus("REFUNDED");
        return cloneOrder(order);
    }

    @Override
    public synchronized MerchantMiniOrderDto rejectRefund(String orderNo, String reason) {
        MerchantMiniOrderDto order = findOrder(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!STATUS_REFUNDING.equals(order.getStatus())) {
            throw new IllegalArgumentException("当前订单状态不可拒绝退款");
        }
        order.setStatus(STATUS_PENDING_VERIFY);
        return cloneOrder(order);
    }

    // ==================== 员工增删改 ====================

    @Override
    public synchronized MerchantMiniStaffUserDto addStaff(MerchantMiniStaffRequestDto requestDto) {
        if (requestDto == null || requestDto.getUsername() == null || requestDto.getUsername().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (roleStaffMap.containsKey(requestDto.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        Long newStaffId = (long) (roleStaffMap.size() + 1);
        MerchantMiniStaffUserDto staffUser = buildStaffUser(
                "merchant_staff_" + newStaffId,
                newStaffId,
                requestDto.getRealName() != null ? requestDto.getRealName() : requestDto.getUsername(),
                requestDto.getPhone(),
                requestDto.getUsername(),
                ROLE_OWNER.equals(requestDto.getRole()) ? "管理员" : "成员",
                buildPermissions(requestDto.getRole())
        );
        roleStaffMap.put(requestDto.getUsername(), staffUser);
        return cloneStaffUser(staffUser);
    }

    @Override
    public synchronized MerchantMiniStaffUserDto updateStaff(MerchantMiniStaffRequestDto requestDto) {
        if (requestDto == null || requestDto.getStaffId() == null) {
            throw new IllegalArgumentException("员工ID不能为空");
        }
        MerchantMiniStaffUserDto target = resolveStaffUserByStaffId(requestDto.getStaffId());
        if (target == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        if (requestDto.getRealName() != null) {
            target.setName(requestDto.getRealName());
        }
        if (requestDto.getPhone() != null) {
            target.setPhone(requestDto.getPhone());
        }
        if (requestDto.getRole() != null) {
            target.setRoleKey(requestDto.getRole());
            target.setRoleName(ROLE_OWNER.equals(requestDto.getRole()) ? "管理员" : "成员");
            target.setPermissions(new ArrayList<>(buildPermissions(requestDto.getRole())));
        }
        return cloneStaffUser(target);
    }

    private List<String> buildPermissions(String role) {
        List<String> permissions = new ArrayList<>();
        permissions.add("stats.view");
        permissions.add("order.manage");
        permissions.add("verify.scan");
        permissions.add("verify.manual");
        permissions.add("verify.record");
        if (ROLE_OWNER.equals(role)) {
            permissions.add("goods.manage");
            permissions.add("store.manage");
            permissions.add("staff.manage");
            permissions.add("finance.manage");
        }
        return permissions;
    }

    private MerchantMiniWorkbenchStatsDto buildStats() {
        MerchantMiniWorkbenchStatsDto statsDto = new MerchantMiniWorkbenchStatsDto();
        int pendingAcceptCount = 0;
        int pendingVerifyCount = 0;
        int completedCount = 0;
        int refundingCount = 0;
        long todaySalesAmount = 0L;

        for (MerchantMiniOrderDto orderDto : orderList) {
            if ("PENDING_ACCEPT".equals(orderDto.getStatus())) {
                pendingAcceptCount++;
            } else if (STATUS_PENDING_VERIFY.equals(orderDto.getStatus())) {
                pendingVerifyCount++;
                todaySalesAmount += orderDto.getPayAmount();
            } else if (STATUS_COMPLETED.equals(orderDto.getStatus())) {
                completedCount++;
                todaySalesAmount += orderDto.getPayAmount();
            } else if (STATUS_REFUNDING.equals(orderDto.getStatus())) {
                refundingCount++;
            }
        }

        statsDto.setPendingAcceptCount(pendingAcceptCount);
        statsDto.setPendingVerifyCount(pendingVerifyCount);
        statsDto.setCompletedCount(completedCount);
        statsDto.setRefundingCount(refundingCount);
        statsDto.setOnShelfCount((int) goodsList.stream().filter((item) -> GOODS_STATUS_ON_SHELF.equals(item.getStatus())).count());
        statsDto.setTodaySalesAmount(todaySalesAmount);
        return statsDto;
    }

    private List<MerchantMiniOrderDto> buildPendingOrderList() {
        List<MerchantMiniOrderDto> result = new ArrayList<>();
        for (MerchantMiniOrderDto orderDto : orderList) {
            if (STATUS_PENDING_VERIFY.equals(orderDto.getStatus())) {
                result.add(cloneOrder(orderDto));
            }
        }
        return result;
    }

    private MerchantMiniOrderDto findOrder(String orderNo) {
        for (MerchantMiniOrderDto orderDto : orderList) {
            if (orderDto.getOrderNo().equals(orderNo)) {
                return orderDto;
            }
        }
        return null;
    }

    private MerchantMiniOrderDto findOrderByVerifyCode(String code) {
        for (MerchantMiniOrderDto orderDto : orderList) {
            if (code.equals(orderDto.getWriteOffCode()) || code.equals(orderDto.getOrderNo())) {
                return orderDto;
            }
        }
        return null;
    }

    private MerchantMiniStaffUserDto resolveStaffUserByRoleKey(String roleKey) {
        if (StringUtils.isBlank(roleKey)) {
            return roleStaffMap.get("manager");
        }
        MerchantMiniStaffUserDto staffUserDto = roleStaffMap.get(roleKey);
        return staffUserDto == null ? roleStaffMap.get("manager") : staffUserDto;
    }

    private MerchantMiniStaffUserDto resolveStaffUserByUserId(String userId) {
        for (MerchantMiniStaffUserDto staffUserDto : roleStaffMap.values()) {
            if (staffUserDto.getUserId().equals(userId)) {
                return staffUserDto;
            }
        }
        return roleStaffMap.get("manager");
    }

    private MerchantMiniStaffUserDto resolveStaffUserByStaffId(Long staffId) {
        for (MerchantMiniStaffUserDto staffUserDto : roleStaffMap.values()) {
            if (staffUserDto.getStaffId().equals(staffId)) {
                return staffUserDto;
            }
        }
        return null;
    }

    private Map<String, MerchantMiniStaffUserDto> initRoleStaffMap() {
        Map<String, MerchantMiniStaffUserDto> result = new LinkedHashMap<>();
        result.put("manager", buildStaffUser("merchant_manager_1", 1L, "林店长", "13800001111", "manager", "店长",
                Arrays.asList("stats.view", "order.manage", "verify.scan", "verify.manual", "goods.manage",
                        "store.manage", "staff.manage", "verify.record", "finance.manage")));
        result.put("clerk", buildStaffUser("merchant_clerk_2", 2L, "周店员", "13800002222", "clerk", "店员",
                Arrays.asList("stats.view", "order.manage", "verify.scan", "verify.manual", "verify.record")));
        return result;
    }

    private MerchantMiniStaffUserDto buildStaffUser(String userId, Long staffId, String name, String phone,
                                                    String roleKey, String roleName, List<String> permissions) {
        MerchantMiniStaffUserDto dto = new MerchantMiniStaffUserDto();
        dto.setUserId(userId);
        dto.setStaffId(staffId);
        dto.setMerchantId(MERCHANT_ID);
        dto.setStoreId(STORE_ID);
        dto.setName(name);
        dto.setPhone(phone);
        dto.setRoleKey(roleKey);
        dto.setRoleName(roleName);
        dto.setStatus(STATUS_ACTIVE);
        dto.setPermissions(new ArrayList<>(permissions));
        dto.setMerchantName("蓝屿轻养生活馆");
        return dto;
    }

    private WxMiniAuthContext buildAuthContext(MerchantMiniStaffUserDto staffUser) {
        WxMiniAuthContext authContext = new WxMiniAuthContext();
        authContext.setUserId(staffUser.getUserId());
        authContext.setUserType(WxMiniAuthContext.USER_TYPE_MERCHANT_STAFF);
        authContext.setStaffId(staffUser.getStaffId());
        authContext.setMerchantId(staffUser.getMerchantId());
        authContext.setStoreId(staffUser.getStoreId());
        authContext.setRoleCodes(Arrays.asList(staffUser.getRoleKey()));
        authContext.setPermissionCodes(staffUser.getPermissions());
        return authContext;
    }

    private MerchantMiniStoreDto initStoreInfo() {
        MerchantMiniStoreDto dto = new MerchantMiniStoreDto();
        dto.setMerchantId(MERCHANT_ID);
        dto.setStoreId(STORE_ID);
        dto.setMerchantName("蓝屿轻养生活馆");
        dto.setStoreName("蓝屿轻养·国贸旗舰店");
        dto.setBrandSlogan("单店团购到店核销运营端");
        dto.setNotice("支持扫码核销、手动核销、商品上下架与单店员工权限管理");
        dto.setBusinessHours("10:00-22:00");
        dto.setPhone("010-88886601");
        dto.setAddress("北京市朝阳区建国路88号嘉里中心B1");
        dto.setServiceTags(Arrays.asList("到店核销", "团购套餐", "营业中"));
        dto.setBannerTitles(Arrays.asList("午市轻养专场", "肩颈护理次卡", "晚间舒压热卖"));
        dto.setBusinessStatus(Boolean.TRUE);
        dto.setSupportRefund(Boolean.TRUE);
        dto.setSupportBooking(Boolean.TRUE);
        return dto;
    }

    private List<MerchantMiniGoodsDto> initGoodsList() {
        List<MerchantMiniGoodsDto> result = new ArrayList<>();
        result.add(buildGoods(101L, "芳香舒压 SPA 90 分钟", "精油舒缓 + 热敷放松 + 独立房间", "SPA轻养",
                "", 19800L, 39800L, 88, 2651, "2026-05-01 至 2026-06-30", "到店出示核销码即可使用",
                GOODS_STATUS_ON_SHELF, 1));
        result.add(buildGoods(102L, "肩颈理疗放松套餐 60 分钟", "久坐人群推荐，到店即用", "肩颈理疗",
                "", 13800L, 26800L, 126, 1942, "2026-05-01 至 2026-07-15", "高峰期建议提前电话确认",
                GOODS_STATUS_ON_SHELF, 2));
        result.add(buildGoods(103L, "都市焕肤护理 75 分钟", "清洁补水 + 舒缓修护", "面部护理",
                "", 16800L, 32800L, 72, 1129, "2026-05-01 至 2026-06-20", "建议提前 1 天预约",
                GOODS_STATUS_OFF_SHELF, 3));
        result.add(buildGoods(104L, "过期测试团购券", "用于验证过期核销拦截", "系统测试",
                "", 9900L, 19800L, 10, 12, "2026-04-01 至 2026-04-30", "过期后不可核销",
                GOODS_STATUS_ON_SHELF, 4));
        return result;
    }

    private MerchantMiniGoodsDto buildGoods(Long goodsId, String title, String subtitle, String categoryName,
                                            String imageUrl, Long price, Long originalPrice, Integer stock, Integer sales,
                                            String validPeriod, String verifyNotice, String status, Integer sort) {
        MerchantMiniGoodsDto dto = new MerchantMiniGoodsDto();
        dto.setGoodsId(goodsId);
        dto.setTitle(title);
        dto.setSubtitle(subtitle);
        dto.setCategoryName(categoryName);
        dto.setImageUrl(imageUrl);
        dto.setPrice(price);
        dto.setOriginalPrice(originalPrice);
        dto.setStock(stock);
        dto.setSales(sales);
        dto.setValidPeriod(validPeriod);
        dto.setVerifyNotice(verifyNotice);
        dto.setStatus(status);
        dto.setSort(sort);
        return dto;
    }

    private List<MerchantMiniOrderDto> initOrderList() {
        List<MerchantMiniOrderDto> result = new ArrayList<>();
        result.add(buildOrder(1L, "M202605090001", 101L, "芳香舒压 SPA 90 分钟", "王女士", "138****2201",
                1, 18800L, STATUS_PENDING_VERIFY, 1778269200000L, 1778269800000L, "LY8012", null, null, null));
        result.add(buildOrder(2L, "M202605090002", 102L, "肩颈理疗放松套餐 60 分钟", "赵先生", "139****3202",
                1, 13800L, STATUS_PENDING_VERIFY, 1778272800000L, 1778273100000L, "LY9321", null, null, null));
        result.add(buildOrder(3L, "M202605080003", 101L, "芳香舒压 SPA 90 分钟", "孙女士", "136****1103",
                1, 18800L, STATUS_COMPLETED, 1778186400000L, 1778187000000L, "LY7710", 1778190000000L, "周店员", null));
        result.add(buildOrder(4L, "M202605070004", 103L, "都市焕肤护理 75 分钟", "何女士", "137****5004",
                1, 16800L, STATUS_REFUNDING, 1778100000000L, 1778100600000L, "LY5508", null, null, "临时无法到店"));
        result.add(buildOrder(5L, "M202604300005", 104L, "过期测试团购券", "陈先生", "135****9005",
                1, 9900L, STATUS_PENDING_VERIFY, 1777564800000L, 1777565400000L, "LY0005", null, null, null));
        return result;
    }

    private MerchantMiniOrderDto buildOrder(Long orderId, String orderNo, Long goodsId, String title,
                                            String customerName, String customerPhone, Integer quantity,
                                            Long payAmount, String status, Long createTime, Long payTime,
                                            String writeOffCode, Long verifyTime, String verifyStaffName,
                                            String refundReason) {
        MerchantMiniOrderDto dto = new MerchantMiniOrderDto();
        dto.setOrderId(orderId);
        dto.setOrderNo(orderNo);
        dto.setGoodsId(goodsId);
        dto.setTitle(title);
        dto.setCustomerName(customerName);
        dto.setCustomerPhone(customerPhone);
        dto.setQuantity(quantity);
        dto.setPayAmount(payAmount);
        dto.setStatus(status);
        dto.setCreateTime(createTime);
        dto.setPayTime(payTime);
        dto.setWriteOffCode(writeOffCode);
        dto.setVerifyTime(verifyTime);
        dto.setVerifyStaffName(verifyStaffName);
        dto.setRefundReason(refundReason);
        return dto;
    }

    private MerchantMiniStaffUserDto cloneStaffUser(MerchantMiniStaffUserDto source) {
        MerchantMiniStaffUserDto dto = new MerchantMiniStaffUserDto();
        dto.setUserId(source.getUserId());
        dto.setStaffId(source.getStaffId());
        dto.setMerchantId(source.getMerchantId());
        dto.setStoreId(source.getStoreId());
        dto.setName(source.getName());
        dto.setPhone(source.getPhone());
        dto.setRoleKey(source.getRoleKey());
        dto.setRoleName(source.getRoleName());
        dto.setStatus(source.getStatus());
        dto.setPermissions(new ArrayList<>(source.getPermissions()));
        dto.setMerchantName(source.getMerchantName());
        return dto;
    }

    private MerchantMiniStoreDto cloneStoreInfo(MerchantMiniStoreDto source) {
        MerchantMiniStoreDto dto = new MerchantMiniStoreDto();
        dto.setMerchantId(source.getMerchantId());
        dto.setStoreId(source.getStoreId());
        dto.setMerchantName(source.getMerchantName());
        dto.setStoreName(source.getStoreName());
        dto.setBrandSlogan(source.getBrandSlogan());
        dto.setNotice(source.getNotice());
        dto.setBusinessHours(source.getBusinessHours());
        dto.setPhone(source.getPhone());
        dto.setAddress(source.getAddress());
        dto.setServiceTags(new ArrayList<>(source.getServiceTags()));
        dto.setBannerTitles(new ArrayList<>(source.getBannerTitles()));
        dto.setBusinessStatus(source.getBusinessStatus());
        dto.setSupportRefund(source.getSupportRefund());
        dto.setSupportBooking(source.getSupportBooking());
        return dto;
    }

    private MerchantMiniOrderDto cloneOrder(MerchantMiniOrderDto source) {
        MerchantMiniOrderDto dto = new MerchantMiniOrderDto();
        dto.setOrderId(source.getOrderId());
        dto.setOrderNo(source.getOrderNo());
        dto.setGoodsId(source.getGoodsId());
        dto.setTitle(source.getTitle());
        dto.setCustomerName(source.getCustomerName());
        dto.setCustomerPhone(source.getCustomerPhone());
        dto.setQuantity(source.getQuantity());
        dto.setPayAmount(source.getPayAmount());
        dto.setStatus(source.getStatus());
        dto.setCreateTime(source.getCreateTime());
        dto.setPayTime(source.getPayTime());
        dto.setWriteOffCode(source.getWriteOffCode());
        dto.setVerifyTime(source.getVerifyTime());
        dto.setVerifyStaffName(source.getVerifyStaffName());
        dto.setRefundReason(source.getRefundReason());
        return dto;
    }

    private MerchantMiniGoodsDto cloneGoods(MerchantMiniGoodsDto source) {
        MerchantMiniGoodsDto dto = new MerchantMiniGoodsDto();
        if (source == null) {
            return dto;
        }
        dto.setGoodsId(source.getGoodsId());
        dto.setTitle(source.getTitle());
        dto.setSubtitle(source.getSubtitle());
        dto.setCategoryName(source.getCategoryName());
        dto.setImageUrl(source.getImageUrl());
        dto.setPrice(source.getPrice());
        dto.setOriginalPrice(source.getOriginalPrice());
        dto.setStock(source.getStock());
        dto.setSales(source.getSales());
        dto.setValidPeriod(source.getValidPeriod());
        dto.setVerifyNotice(source.getVerifyNotice());
        dto.setStatus(source.getStatus());
        dto.setSort(source.getSort());
        return dto;
    }

    private MerchantMiniGoodsDto findGoods(Long goodsId) {
        for (MerchantMiniGoodsDto goodsDto : goodsList) {
            if (goodsDto.getGoodsId().equals(goodsId)) {
                return goodsDto;
            }
        }
        return null;
    }

    private Long nextGoodsId() {
        long maxId = 100L;
        for (MerchantMiniGoodsDto goodsDto : goodsList) {
            if (goodsDto.getGoodsId() != null && goodsDto.getGoodsId() > maxId) {
                maxId = goodsDto.getGoodsId();
            }
        }
        return maxId + 1L;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private List<MerchantMiniStaffUserDto> cloneStaffList() {
        List<MerchantMiniStaffUserDto> result = new ArrayList<>();
        for (MerchantMiniStaffUserDto staffUserDto : roleStaffMap.values()) {
            result.add(cloneStaffUser(staffUserDto));
        }
        return result;
    }

    private <T> List<T> safeList(List<T> source) {
        return source == null ? new ArrayList<>() : source;
    }

    private List<MerchantMiniVerifyRecordDto> initVerifyRecordList() {
        List<MerchantMiniVerifyRecordDto> result = new ArrayList<>();
        for (MerchantMiniOrderDto orderDto : orderList) {
            if (STATUS_COMPLETED.equals(orderDto.getStatus())) {
                MerchantMiniStaffUserDto staffUser = resolveStaffUserByRoleKey("clerk");
                result.add(buildVerifyRecord(nextVerifyRecordId(result), orderDto, orderDto.getWriteOffCode(), staffUser,
                        VERIFY_STATUS_SUCCESS, null, orderDto.getVerifyTime()));
            }
        }
        return result;
    }

    private void addVerifyRecord(MerchantMiniOrderDto orderDto, String inputCode, MerchantMiniStaffUserDto staffUser,
                                 String status, String failureReason) {
        verifyRecordList.add(0, buildVerifyRecord(nextVerifyRecordId(verifyRecordList), orderDto, inputCode, staffUser,
                status, failureReason, System.currentTimeMillis()));
    }

    private MerchantMiniVerifyRecordDto buildVerifyRecord(Long recordId, MerchantMiniOrderDto orderDto, String inputCode,
                                                          MerchantMiniStaffUserDto staffUser, String status,
                                                          String failureReason, Long verifyTime) {
        MerchantMiniVerifyRecordDto dto = new MerchantMiniVerifyRecordDto();
        dto.setRecordId(recordId);
        dto.setInputCode(inputCode);
        dto.setStatus(status);
        dto.setFailureReason(failureReason);
        dto.setVerifyTime(verifyTime);
        if (staffUser != null) {
            dto.setVerifyStaffId(staffUser.getStaffId());
            dto.setVerifyStaffName(staffUser.getName());
        }
        if (orderDto != null) {
            dto.setOrderNo(orderDto.getOrderNo());
            dto.setGoodsId(orderDto.getGoodsId());
            dto.setTitle(orderDto.getTitle());
            dto.setWriteOffCode(orderDto.getWriteOffCode());
            dto.setCustomerName(orderDto.getCustomerName());
            dto.setCustomerPhone(orderDto.getCustomerPhone());
            dto.setPayAmount(orderDto.getPayAmount());
        } else {
            dto.setTitle("未知核销码");
            dto.setPayAmount(0L);
        }
        return dto;
    }

    private Long nextVerifyRecordId(List<MerchantMiniVerifyRecordDto> sourceList) {
        long maxId = 0L;
        for (MerchantMiniVerifyRecordDto recordDto : sourceList) {
            if (recordDto.getRecordId() != null && recordDto.getRecordId() > maxId) {
                maxId = recordDto.getRecordId();
            }
        }
        return maxId + 1L;
    }

    private MerchantMiniVerifyRecordDto cloneVerifyRecord(MerchantMiniVerifyRecordDto source) {
        MerchantMiniVerifyRecordDto dto = new MerchantMiniVerifyRecordDto();
        dto.setRecordId(source.getRecordId());
        dto.setOrderNo(source.getOrderNo());
        dto.setGoodsId(source.getGoodsId());
        dto.setTitle(source.getTitle());
        dto.setInputCode(source.getInputCode());
        dto.setWriteOffCode(source.getWriteOffCode());
        dto.setCustomerName(source.getCustomerName());
        dto.setCustomerPhone(source.getCustomerPhone());
        dto.setPayAmount(source.getPayAmount());
        dto.setStatus(source.getStatus());
        dto.setFailureReason(source.getFailureReason());
        dto.setVerifyTime(source.getVerifyTime());
        dto.setVerifyStaffId(source.getVerifyStaffId());
        dto.setVerifyStaffName(source.getVerifyStaffName());
        return dto;
    }

    private boolean isGoodsExpired(MerchantMiniOrderDto orderDto) {
        MerchantMiniGoodsDto goodsDto = findGoods(orderDto.getGoodsId());
        if (goodsDto == null || StringUtils.isBlank(goodsDto.getValidPeriod())) {
            return false;
        }
        Long expireTime = parseValidPeriodEndTime(goodsDto.getValidPeriod());
        return expireTime != null && System.currentTimeMillis() > expireTime;
    }

    private Long parseValidPeriodEndTime(String validPeriod) {
        String[] parts = validPeriod.split("至");
        String endDateText = parts.length == 0 ? validPeriod.trim() : parts[parts.length - 1].trim();
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(endDateText).getTime() + DAY_MILLIS - 1L;
        } catch (ParseException e) {
            return null;
        }
    }

    private List<MerchantMiniFinanceLedgerDto> buildFinanceLedgerList() {
        List<MerchantMiniFinanceLedgerDto> result = new ArrayList<>();
        long ledgerId = 1L;
        for (MerchantMiniOrderDto orderDto : orderList) {
            if (!STATUS_COMPLETED.equals(orderDto.getStatus())) {
                continue;
            }
            Long finishTime = orderDto.getVerifyTime() == null ? orderDto.getPayTime() : orderDto.getVerifyTime();
            Long settleTime = defaultLong(finishTime) + DAY_MILLIS;
            MerchantMiniFinanceLedgerDto dto = new MerchantMiniFinanceLedgerDto();
            dto.setLedgerId(ledgerId++);
            dto.setOrderNo(orderDto.getOrderNo());
            dto.setTitle(orderDto.getTitle());
            dto.setOrderAmount(orderDto.getPayAmount());
            dto.setMerchantAmount(orderDto.getPayAmount() * MERCHANT_RATE / 100);
            dto.setPlatformFeeAmount(orderDto.getPayAmount() * PLATFORM_RATE / 100);
            dto.setFinishTime(finishTime);
            dto.setSettleTime(settleTime);
            dto.setStatus(settleTime <= System.currentTimeMillis() ? LEDGER_STATUS_SETTLED : LEDGER_STATUS_PENDING);
            result.add(dto);
        }
        result.sort(Comparator.comparing(MerchantMiniFinanceLedgerDto::getFinishTime, Comparator.nullsLast(Long::compareTo)).reversed());
        return result;
    }

    private MerchantMiniSettlementAccountDto buildSettlementAccount() {
        MerchantMiniSettlementAccountDto dto = new MerchantMiniSettlementAccountDto();
        dto.setAccountName(storeInfo.getStoreName());
        dto.setBankName("招商银行");
        dto.setAccountNoTail("6601");
        dto.setStatus("VERIFIED");
        return dto;
    }

    private MerchantMiniSettlementRecordDto buildSettlementRecord(MerchantMiniFinanceLedgerDto ledgerDto) {
        MerchantMiniSettlementRecordDto dto = new MerchantMiniSettlementRecordDto();
        dto.setSettlementId("S" + ledgerDto.getOrderNo());
        dto.setOrderNo(ledgerDto.getOrderNo());
        dto.setTitle(ledgerDto.getTitle());
        dto.setAmount(ledgerDto.getMerchantAmount());
        dto.setApplyTime(ledgerDto.getFinishTime());
        dto.setExpectedTransferTime(ledgerDto.getSettleTime());
        if (LEDGER_STATUS_SETTLED.equals(ledgerDto.getStatus())) {
            dto.setStatus(SETTLEMENT_STATUS_ARRIVED);
            dto.setArriveTime(ledgerDto.getSettleTime());
            dto.setRemark("微信已自动打款至结算卡");
        } else {
            dto.setStatus(SETTLEMENT_STATUS_WAITING_T1);
            dto.setRemark("订单完成后进入 T+1 自动打款队列");
        }
        return dto;
    }

    private Long nextAutoTransferTime() {
        long now = System.currentTimeMillis();
        long todayStart = now - (now + 8 * 60 * 60 * 1000L) % DAY_MILLIS;
        long next = todayStart + DAY_MILLIS + 10 * 60 * 60 * 1000L;
        return next <= now ? next + DAY_MILLIS : next;
    }

    private List<MerchantMiniWithdrawRecordDto> initWithdrawRecordList() {
        return new ArrayList<>();
    }

    private List<MerchantMiniWithdrawRecordDto> cloneWithdrawRecordList() {
        List<MerchantMiniWithdrawRecordDto> result = new ArrayList<>();
        for (MerchantMiniWithdrawRecordDto recordDto : withdrawRecordList) {
            result.add(cloneWithdrawRecord(recordDto));
        }
        result.sort((a, b) -> Long.compare(defaultLong(b.getApplyTime()), defaultLong(a.getApplyTime())));
        return result;
    }

    private MerchantMiniWithdrawRecordDto cloneWithdrawRecord(MerchantMiniWithdrawRecordDto source) {
        MerchantMiniWithdrawRecordDto dto = new MerchantMiniWithdrawRecordDto();
        dto.setWithdrawId(source.getWithdrawId());
        dto.setAmount(source.getAmount());
        dto.setStatus(source.getStatus());
        dto.setApplyTime(source.getApplyTime());
        dto.setFinishTime(source.getFinishTime());
        dto.setRemark(source.getRemark());
        return dto;
    }

    private Long nextWithdrawId() {
        long maxId = 0L;
        for (MerchantMiniWithdrawRecordDto recordDto : withdrawRecordList) {
            if (recordDto.getWithdrawId() != null && recordDto.getWithdrawId() > maxId) {
                maxId = recordDto.getWithdrawId();
            }
        }
        return maxId + 1L;
    }

    private boolean isToday(Long time) {
        if (time == null) {
            return false;
        }
        String today = new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());
        return today.equals(new SimpleDateFormat("yyyyMMdd").format(time));
    }

    private boolean isCurrentMonth(Long time) {
        if (time == null) {
            return false;
        }
        String month = new SimpleDateFormat("yyyyMM").format(System.currentTimeMillis());
        return month.equals(new SimpleDateFormat("yyyyMM").format(time));
    }
}
