package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.order.domain.BookingService;
import com.ruoyi.mall.order.mapper.BookingServiceMapper;
import com.ruoyi.mall.order.constant.BookingStatus;
import com.ruoyi.mall.order.domain.BookingRecord;
import com.ruoyi.mall.order.service.IBookingRecordService;
import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.service.IProductService;
import com.ruoyi.mall.user.domain.UserInfo;
import com.ruoyi.mall.user.service.IUserInfoService;
import com.ruoyi.wxmini.dto.wx.WxBookingCreateRequestDto;
import com.ruoyi.wxmini.dto.wx.WxBookingRecordDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/wxmini/booking")
public class WxBookingController {

    @Resource
    private IBookingRecordService bookingRecordService;
    @Resource
    private BookingServiceMapper bookingServiceMapper;
    @Resource
    private IProductService productService;
    @Resource
    private IUserInfoService userInfoService;

    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String status) {
        Long currentUserPk = resolveCurrentUserPk(WxMiniUserContext.getCurrentUserId());
        if (currentUserPk == null) {
            return AjaxResult.success(new ArrayList<>());
        }
        bookingRecordService.markExpiredPendingByUser(currentUserPk);

        BookingRecord query = new BookingRecord();
        query.setUserId(currentUserPk);
        if (StringUtils.isNotBlank(status) && !"ALL".equalsIgnoreCase(status)) {
            query.setStatus(status);
        }

        List<BookingRecord> records = bookingRecordService.selectBookingRecordList(query);
        List<WxBookingRecordDto> result = new ArrayList<>();
        for (BookingRecord record : records) {
            result.add(convertToDto(record));
        }
        return AjaxResult.success(result);
    }

    @PostMapping("/create")
    public AjaxResult create(@Valid @RequestBody WxBookingCreateRequestDto requestDto) {
        String currentUserId = WxMiniUserContext.getCurrentUserId();
        if (StringUtils.isBlank(currentUserId)) {
            return AjaxResult.error("请先登录");
        }
        Long currentUserPk = resolveCurrentUserPk(currentUserId);
        if (currentUserPk == null) {
            return AjaxResult.error("用户信息不存在");
        }

        BookingService bookingService = selectBookingServiceByEntryIdSafely(requestDto.getProductId());
        if (bookingService != null && (bookingService.getStatus() == null || bookingService.getStatus() != 1)) {
            return AjaxResult.error("服务不存在或已下架");
        }

        Product product = null;
        if (bookingService != null && bookingService.getProductId() != null) {
            product = productService.selectProductById(bookingService.getProductId());
        }
        if (product == null) {
            product = productService.selectProductById(requestDto.getProductId());
        }
        if (bookingService == null && (product == null || product.getStatus() == null || product.getStatus() != 1)) {
            return AjaxResult.error("服务不存在或已下架");
        }

        Long currentMerchantId = WxMiniUserContext.getCurrentMerchantId();
        if (currentMerchantId == null) {
            currentMerchantId = WxMiniUserContext.getAppIdMerchantId();
        }
        Long serviceMerchantId = bookingService != null ? bookingService.getMerchantId() : product.getMerchantId();
        if (currentMerchantId != null && !currentMerchantId.equals(serviceMerchantId)) {
            return AjaxResult.error("服务不属于当前商家");
        }

        Date bookingTime = new Date(requestDto.getBookingTime());
        if (bookingTime.before(new Date())) {
            return AjaxResult.error("预约时间不能早于当前时间");
        }

        BookingRecord record = new BookingRecord();
        record.setBookingNo(generateBookingNo());
        record.setMerchantId(serviceMerchantId);
        record.setUserId(currentUserPk);
        record.setBookingServiceId(bookingService != null ? bookingService.getId() : null);
        record.setProductId(resolveBookingProductId(requestDto.getProductId(), bookingService, product));
        record.setProductName(resolveBookingServiceName(bookingService, product));
        record.setProductImage(resolveBookingServiceImage(bookingService, product));
        record.setProductPrice(resolveBookingServicePrice(bookingService, product));
        record.setBookingTime(bookingTime);
        record.setContactName(StringUtils.trimToEmpty(requestDto.getContactName()));
        record.setContactPhone(StringUtils.trimToEmpty(requestDto.getContactPhone()));
        record.setPeopleCount(normalizePeopleCount(requestDto.getPeopleCount()));
        record.setRemark(StringUtils.trimToEmpty(requestDto.getRemark()));
        record.setStatus(BookingStatus.PENDING);
        record.setExpireTime(buildDefaultExpireTime(bookingTime));
        record.setDelFlag("0");
        bookingRecordService.insertBookingRecord(record);

        return AjaxResult.success(convertToDto(record));
    }

    private BookingService selectBookingServiceByEntryIdSafely(Long id) {
        if (id == null) {
            return null;
        }
        try {
            BookingService bookingService = bookingServiceMapper.selectBookingServiceById(id);
            if (bookingService != null) {
                return bookingService;
            }
            return bookingServiceMapper.selectBookingServiceByProductId(id);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long resolveBookingProductId(Long requestId, BookingService bookingService, Product product) {
        if (bookingService != null && bookingService.getProductId() != null) {
            return bookingService.getProductId();
        }
        if (product != null) {
            return product.getId();
        }
        return bookingService == null ? requestId : null;
    }

    private String resolveBookingServiceName(BookingService bookingService, Product product) {
        if (bookingService != null && StringUtils.isNotBlank(bookingService.getServiceName())) {
            return bookingService.getServiceName();
        }
        return product != null ? product.getName() : "预约服务";
    }

    private String resolveBookingServiceImage(BookingService bookingService, Product product) {
        if (bookingService != null && StringUtils.isNotBlank(bookingService.getServiceImage())) {
            return bookingService.getServiceImage();
        }
        return product != null ? product.getCoverImage() : "";
    }

    private BigDecimal resolveBookingServicePrice(BookingService bookingService, Product product) {
        if (bookingService != null && bookingService.getServicePrice() != null) {
            return bookingService.getServicePrice();
        }
        return product != null ? product.getPrice() : BigDecimal.ZERO;
    }

    @PostMapping("/cancel/{bookingNo}")
    public AjaxResult cancel(@PathVariable String bookingNo) {
        String currentUserId = WxMiniUserContext.getCurrentUserId();
        Long currentUserPk = resolveCurrentUserPk(currentUserId);
        if (currentUserPk == null) {
            return AjaxResult.error("请先登录");
        }

        BookingRecord record = bookingRecordService.selectBookingRecordByBookingNo(bookingNo);
        if (record == null || record.getUserId() == null || !record.getUserId().equals(currentUserPk)) {
            return AjaxResult.error("预约记录不存在");
        }
        if (!BookingStatus.PENDING.equals(record.getStatus()) && !BookingStatus.CONFIRMED.equals(record.getStatus())) {
            return AjaxResult.error("当前预约状态不可取消");
        }

        bookingRecordService.updateBookingStatus(bookingNo, BookingStatus.CANCELLED);
        return AjaxResult.success(convertToDto(bookingRecordService.selectBookingRecordByBookingNo(bookingNo)));
    }

    private Long resolveCurrentUserPk(String currentUserId) {
        if (StringUtils.isBlank(currentUserId)) {
            return null;
        }
        UserInfo userInfo = userInfoService.selectUserInfoByUserId(currentUserId);
        return userInfo != null ? userInfo.getId() : null;
    }

    private int normalizePeopleCount(Integer peopleCount) {
        if (peopleCount == null || peopleCount < 1) {
            return 1;
        }
        return Math.min(peopleCount, 99);
    }

    private Date buildDefaultExpireTime(Date bookingTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(bookingTime);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    private WxBookingRecordDto convertToDto(BookingRecord record) {
        WxBookingRecordDto dto = new WxBookingRecordDto();
        dto.setId(record.getId());
        dto.setBookingNo(record.getBookingNo());
        dto.setMerchantId(record.getMerchantId());
        dto.setProductId(record.getProductId());
        dto.setTitle(record.getProductName());
        dto.setImage(appendListThumb(record.getProductImage()));
        dto.setPrice(toFen(record.getProductPrice()));
        dto.setBookingTime(toMillis(record.getBookingTime()));
        dto.setContactName(record.getContactName());
        dto.setContactPhone(record.getContactPhone());
        dto.setPeopleCount(record.getPeopleCount());
        dto.setStatus(record.getStatus());
        dto.setRemark(record.getRemark());
        dto.setCreateTime(toMillis(record.getCreateTime()));
        dto.setConfirmTime(toMillis(record.getConfirmTime()));
        dto.setCompleteTime(toMillis(record.getCompleteTime()));
        dto.setCancelTime(toMillis(record.getCancelTime()));
        dto.setExpireTime(toMillis(record.getExpireTime()));
        return dto;
    }

    private Long toMillis(Date date) {
        return date != null ? date.getTime() : null;
    }

    private long toFen(BigDecimal amount) {
        return amount != null ? amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact() : 0L;
    }

    private String appendListThumb(String imageUrl) {
        if (StringUtils.isBlank(imageUrl)
                || (!imageUrl.contains("/profile/merchant_images/") && !imageUrl.contains("/profile/merchant-goods/"))
                || imageUrl.contains("?thumb=")
                || imageUrl.contains("&thumb=")) {
            return imageUrl;
        }
        return imageUrl + (imageUrl.contains("?") ? "&" : "?") + "thumb=list";
    }

    private String generateBookingNo() {
        String datePart = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "BKG" + datePart + randomPart;
    }
}
