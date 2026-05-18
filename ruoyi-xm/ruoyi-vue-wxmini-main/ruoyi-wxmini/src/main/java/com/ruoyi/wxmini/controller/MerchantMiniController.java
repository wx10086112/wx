package com.ruoyi.wxmini.controller;

import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniGoodsDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniGoodsStatusRequestDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniLoginRequestDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniStaffPermissionRequestDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniStoreDto;
import com.ruoyi.wxmini.dto.merchant.MerchantMiniWithdrawRequestDto;
import com.ruoyi.wxmini.service.IMerchantMiniMockService;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

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

    @PostMapping("/auth/login")
    public AjaxResult login(@RequestBody(required = false) MerchantMiniLoginRequestDto requestDto) {
        String roleKey = requestDto == null ? null : requestDto.getRoleKey();
        return AjaxResult.success(merchantMiniMockService.login(roleKey));
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
        AjaxResult accessDenied = checkAccess(PERMISSION_VERIFY_SCAN, PERMISSION_VERIFY_MANUAL);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniMockService.writeOff(code, WxMiniUserContext.getCurrentUserId()));
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
        AjaxResult accessDenied = checkAccess(PERMISSION_GOODS_MANAGE);
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
        AjaxResult accessDenied = checkAccess(PERMISSION_GOODS_MANAGE);
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
        AjaxResult accessDenied = checkAccess(PERMISSION_GOODS_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        String fileName = file == null ? null : file.getOriginalFilename();
        Long size = file == null ? 0L : file.getSize();
        return AjaxResult.success(merchantMiniMockService.uploadGoodsImage(fileName, size));
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
        AjaxResult accessDenied = checkAccess(PERMISSION_STORE_MANAGE);
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
        AjaxResult accessDenied = checkAccess(PERMISSION_STAFF_MANAGE);
        if (accessDenied != null) {
            return accessDenied;
        }
        try {
            return AjaxResult.success(merchantMiniMockService.updateStaffPermission(requestDto));
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
        AjaxResult accessDenied = checkAccess(PERMISSION_FINANCE_MANAGE);
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

    private AjaxResult checkAccess(String... permissionCodes) {
        if (!WxMiniUserContext.isMerchantStaff()) {
            return AjaxResult.error(HttpStatus.FORBIDDEN, "仅商家员工可访问");
        }
        if (!WxMiniUserContext.hasAnyPermission(permissionCodes)) {
            return AjaxResult.error(HttpStatus.FORBIDDEN, "无权限");
        }
        return null;
    }
}
