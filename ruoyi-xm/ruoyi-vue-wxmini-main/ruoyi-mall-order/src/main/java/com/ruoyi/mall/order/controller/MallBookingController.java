package com.ruoyi.mall.order.controller;

import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.mall.order.constant.BookingStatus;
import com.ruoyi.mall.order.domain.BookingRecord;
import com.ruoyi.mall.order.service.IBookingRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/mall/booking")
public class MallBookingController extends BaseController {

    @Autowired
    private IBookingRecordService bookingRecordService;

    @DataScopeBiz(merchantAlias = "b", distributorAlias = "m")
    @PreAuthorize("@ss.hasPermi('mall:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(BookingRecord query) {
        bookingRecordService.markExpiredPending();
        startPage();
        List<BookingRecord> list = bookingRecordService.selectBookingRecordList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:order:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        BookingRecord record = bookingRecordService.selectBookingRecordById(id);
        AjaxResult denied = checkBookingAccess(record);
        if (denied != null) {
            return denied;
        }
        return success(record);
    }

    @PreAuthorize("@ss.hasPermi('mall:order:edit')")
    @PostMapping("/confirm/{id}")
    public AjaxResult confirm(@PathVariable Long id) {
        return updateStatus(id, BookingStatus.CONFIRMED, BookingStatus.PENDING);
    }

    @PreAuthorize("@ss.hasPermi('mall:order:edit')")
    @PostMapping("/complete/{id}")
    public AjaxResult complete(@PathVariable Long id) {
        return updateStatus(id, BookingStatus.COMPLETED, BookingStatus.CONFIRMED);
    }

    @PreAuthorize("@ss.hasPermi('mall:order:edit')")
    @PostMapping("/cancel/{id}")
    public AjaxResult cancel(@PathVariable Long id) {
        return updateStatus(id, BookingStatus.CANCELLED, BookingStatus.PENDING, BookingStatus.CONFIRMED);
    }

    @PreAuthorize("@ss.hasPermi('mall:order:edit')")
    @PostMapping("/expire/{id}")
    public AjaxResult expire(@PathVariable Long id) {
        return updateStatus(id, BookingStatus.EXPIRED, BookingStatus.PENDING);
    }

    private AjaxResult updateStatus(Long id, String targetStatus, String... allowedStatuses) {
        BookingRecord record = bookingRecordService.selectBookingRecordById(id);
        AjaxResult denied = checkBookingAccess(record);
        if (denied != null) {
            return denied;
        }
        if (!Arrays.asList(allowedStatuses).contains(record.getStatus())) {
            return error("当前预点单状态不可操作");
        }
        return toAjax(bookingRecordService.updateBookingStatus(record.getBookingNo(), targetStatus));
    }

    private AjaxResult checkBookingAccess(BookingRecord record) {
        if (record == null) {
            return error("预点单记录不存在");
        }
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null && !effMerchantId.equals(record.getMerchantId())) {
            return error("无权限查看该预点单记录");
        }
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !effDistributorId.equals(record.getDistributorId())) {
            return error("无权限查看该预点单记录");
        }
        return null;
    }
}
