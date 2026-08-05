package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.service.IWxPayOrderService;
import com.ruoyi.mall.common.util.WriteOffCodeGenerator;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.constant.MallOrderStatus;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderStatusHistory;
import com.ruoyi.mall.order.domain.OrderItem;
import com.ruoyi.mall.order.domain.RefundRecord;
import com.ruoyi.mall.order.mapper.RefundRecordMapper;
import com.ruoyi.mall.order.service.IMallOrderService;
import com.ruoyi.mall.pay.domain.PaymentRecord;
import com.ruoyi.mall.pay.service.IPaymentRecordService;
import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.service.IProductService;
import com.ruoyi.mall.user.domain.UserInfo;
import com.ruoyi.mall.user.service.IUserInfoService;
import com.ruoyi.wxmini.dto.wx.WxOrderCreateRequestDto;
import com.ruoyi.wxmini.dto.wx.WxOrderDto;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/wxmini/order")
public class WxOrderController {

    private static final int MAX_WRITE_OFF_CODE_RETRIES = 10;
    private static final long PENDING_EXPIRE_MILLIS = 30 * 60 * 1000L;

    @Resource
    private IMallOrderService mallOrderService;
    @Resource
    private RefundRecordMapper refundRecordMapper;
    @Resource
    private IProductService productService;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private WriteOffCodeGenerator writeOffCodeGenerator;
    @Resource
    private IWxPayOrderService wxPayOrderService;
    @Resource
    private IPaymentRecordService paymentRecordService;
    @Resource
    private IUserInfoService userInfoService;

    @PostMapping("/create")
    public AjaxResult create(@Valid @RequestBody WxOrderCreateRequestDto requestDto) {
        if (requestDto == null) {
            return AjaxResult.error("请求参数不能为空");
        }

        String currentUserId = WxMiniUserContext.getCurrentUserId();
        if (StringUtils.isBlank(currentUserId)) {
            return AjaxResult.error("请先登录");
        }
        Long userId = resolveCurrentUserPk(currentUserId);
        if (userId == null) {
            return AjaxResult.error("invalid user");
        }

        List<WxOrderCreateRequestDto.OrderItemInput> itemInputs = buildItemInputs(requestDto);
        if (itemInputs.isEmpty()) {
            return AjaxResult.error("至少选择一个商品");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Long merchantId = null;
        List<Product> products = new ArrayList<>();
        for (WxOrderCreateRequestDto.OrderItemInput input : itemInputs) {
            Product product = productService.selectProductById(input.getProductId());
            if (product == null || product.getStatus() == null || product.getStatus() != 1) {
                return AjaxResult.error("商品不存在或已下架: " + input.getProductId());
            }

            if (merchantId == null) {
                merchantId = product.getMerchantId();
            } else if (!merchantId.equals(product.getMerchantId())) {
                return AjaxResult.error("不同商家的商品不能合并下单");
            }

            int quantity = normalizeQuantity(input.getQuantity());
            if (product.getStock() == null || product.getStock() < quantity) {
                return AjaxResult.error("商品库存不足: " + product.getName());
            }

            BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(quantity)));
            products.add(product);
        }

        Long currentMerchantId = WxMiniUserContext.getCurrentMerchantId();
        if (currentMerchantId == null) {
            currentMerchantId = WxMiniUserContext.getAppIdMerchantId();
        }
        if (currentMerchantId != null && !currentMerchantId.equals(merchantId)) {
            return AjaxResult.error("商品不属于当前商家");
        }

        BigDecimal couponAmount = BigDecimal.ZERO;
        BigDecimal payAmount = totalAmount.subtract(couponAmount);
        String orderNo = generateOrderNo();
        String writeOffCode;
        try {
            writeOffCode = generateUniqueWriteOffCode();
        } catch (IllegalStateException e) {
            return AjaxResult.error("核销码生成失败，请稍后重试");
        }

        MallOrder order = new MallOrder();
        order.setOrderNo(orderNo);
        order.setMerchantId(merchantId);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setCouponAmount(couponAmount);
        order.setCouponId(requestDto.getCouponId());
        order.setStatus(MallOrderStatus.PENDING);
        order.setWriteOffCode(writeOffCode);

        List<OrderItem> orderItems = new ArrayList<>();
        for (int i = 0; i < itemInputs.size(); i++) {
            WxOrderCreateRequestDto.OrderItemInput input = itemInputs.get(i);
            Product product = products.get(i);
            int quantity = normalizeQuantity(input.getQuantity());
            BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;

            OrderItem item = new OrderItem();
            item.setOrderNo(orderNo);
            item.setMerchantId(merchantId);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getCoverImage());
            item.setPrice(price);
            item.setQuantity(quantity);
            item.setSubtotal(price.multiply(BigDecimal.valueOf(quantity)));
            orderItems.add(item);
        }

        try {
            mallOrderService.createOrderWithItems(order, orderItems);
        } catch (RuntimeException e) {
            return AjaxResult.error(e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("orderAmount", toFen(totalAmount));
        result.put("couponAmount", toFen(couponAmount));
        result.put("payAmount", toFen(payAmount));
        return AjaxResult.success(result);
    }

    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String status) {
        String currentUserId = WxMiniUserContext.getCurrentUserId();
        if (StringUtils.isBlank(currentUserId)) {
            return AjaxResult.success(new ArrayList<>());
        }

        MallOrder query = new MallOrder();
        Long currentUserPk = resolveCurrentUserPk(currentUserId);
        if (currentUserPk == null) {
            return AjaxResult.success(new ArrayList<>());
        }
        Long currentMerchantId = getCurrentMerchantId();
        if (currentMerchantId == null) {
            return AjaxResult.error("当前小程序登录态缺少商户信息");
        }
        query.setUserId(currentUserPk);
        query.setMerchantId(currentMerchantId);
        List<MallOrder> orders = mallOrderService.selectMallOrderList(query);

        Map<Long, String> merchantNameCache = new HashMap<>();
        List<WxOrderDto> result = new ArrayList<>();
        for (MallOrder order : orders) {
            String wxStatus = mapDbStatus(order.getStatus());
            if (StringUtils.isNotBlank(status) && !status.equals(wxStatus)) {
                continue;
            }
            result.add(convertToDto(order, merchantNameCache));
        }
        result.sort((a, b) -> Long.compare(b.getCreateTime(), a.getCreateTime()));
        return AjaxResult.success(result);
    }

    @GetMapping("/detail/{orderNo}")
    public AjaxResult detail(@PathVariable String orderNo) {
        String currentUserId = WxMiniUserContext.getCurrentUserId();
        if (StringUtils.isBlank(currentUserId)) {
            return AjaxResult.error("请先登录");
        }

        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        if (order == null || !isCurrentUserOrder(order, currentUserId) || !isCurrentMerchantOrder(order)) {
            return AjaxResult.error("订单不存在");
        }
        return AjaxResult.success(convertToDto(order, new HashMap<>()));
    }

    @PostMapping("/cancel/{orderNo}")
    public AjaxResult cancel(@PathVariable String orderNo) {
        String currentUserId = WxMiniUserContext.getCurrentUserId();
        if (StringUtils.isBlank(currentUserId)) {
            return AjaxResult.error("请先登录");
        }

        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        if (order == null || !isCurrentUserOrder(order, currentUserId) || !isCurrentMerchantOrder(order)) {
            return AjaxResult.error("订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != MallOrderStatus.PENDING) {
            return AjaxResult.error("当前订单状态不可取消");
        }

        boolean cancelled;
        PaymentRecord paymentRecord = paymentRecordService.selectByOrderNo(orderNo);
        try {
            if (paymentRecord != null) {
                cancelled = Boolean.TRUE.equals(wxPayOrderService.cancelOrder(currentUserId, orderNo));
            } else {
                cancelled = mallOrderService.cancelPendingOrder(orderNo);
            }
        } catch (Exception e) {
            return AjaxResult.error("取消订单失败: " + e.getMessage());
        }

        MallOrder latestOrder = mallOrderService.selectMallOrderByOrderNo(orderNo);
        if (!cancelled || latestOrder == null || latestOrder.getStatus() == null
                || latestOrder.getStatus() != MallOrderStatus.CANCELLED) {
            return AjaxResult.error("当前订单不可取消");
        }
        return AjaxResult.success(convertToDto(latestOrder, new HashMap<>()));
    }

    private boolean isCurrentUserOrder(MallOrder order, String currentUserId) {
        Long currentUserPk = resolveCurrentUserPk(currentUserId);
        return currentUserPk != null && order.getUserId() != null && order.getUserId().equals(currentUserPk);
    }

    private boolean isCurrentMerchantOrder(MallOrder order) {
        Long merchantId = getCurrentMerchantId();
        return merchantId != null && order.getMerchantId() != null && merchantId.equals(order.getMerchantId());
    }

    private Long getCurrentMerchantId() {
        Long merchantId = WxMiniUserContext.getCurrentMerchantId();
        return merchantId != null ? merchantId : WxMiniUserContext.getAppIdMerchantId();
    }

    private Long resolveCurrentUserPk(String currentUserId) {
        if (StringUtils.isBlank(currentUserId)) {
            return null;
        }
        UserInfo userInfo = userInfoService.selectUserInfoByUserId(currentUserId);
        return userInfo != null ? userInfo.getId() : null;
    }

    private List<WxOrderCreateRequestDto.OrderItemInput> buildItemInputs(WxOrderCreateRequestDto requestDto) {
        Map<Long, Integer> quantityByProduct = new LinkedHashMap<>();
        if (requestDto.getItems() != null && !requestDto.getItems().isEmpty()) {
            for (WxOrderCreateRequestDto.OrderItemInput input : requestDto.getItems()) {
                if (input == null || input.getProductId() == null) {
                    continue;
                }
                int quantity = normalizeQuantity(input.getQuantity());
                quantityByProduct.merge(input.getProductId(), quantity, Integer::sum);
            }
        } else if (requestDto.getProductId() != null) {
            quantityByProduct.put(requestDto.getProductId(), normalizeQuantity(requestDto.getQuantity()));
        }

        List<WxOrderCreateRequestDto.OrderItemInput> itemInputs = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : quantityByProduct.entrySet()) {
            WxOrderCreateRequestDto.OrderItemInput input = new WxOrderCreateRequestDto.OrderItemInput();
            input.setProductId(entry.getKey());
            input.setQuantity(entry.getValue());
            itemInputs.add(input);
        }
        return itemInputs;
    }

    private int normalizeQuantity(Integer quantity) {
        return quantity != null && quantity > 0 ? quantity : 1;
    }

    private WxOrderDto convertToDto(MallOrder order, Map<Long, String> merchantNameCache) {
        WxOrderDto dto = new WxOrderDto();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setMerchantId(order.getMerchantId());
        dto.setWriteOffCode(order.getWriteOffCode());
        dto.setOrderAmount(toFen(order.getTotalAmount()));
        dto.setCouponAmount(toFen(order.getCouponAmount()));
        dto.setPayAmount(toFen(order.getPayAmount()));
        dto.setCreateTime(order.getCreateTime() != null ? order.getCreateTime().getTime() : 0L);
        dto.setPayTime(order.getPayTime() != null ? order.getPayTime().getTime() : null);
        dto.setWriteOffTime(order.getUseTime() != null ? order.getUseTime().getTime() : null);
        dto.setRefundTime(order.getRefundTime() != null ? order.getRefundTime().getTime() : null);
        dto.setQuantity(1);

        String wxStatus = mapDbStatus(order.getStatus());
        RefundRecord latestRefund = null;
        try {
            RefundRecord refundQuery = new RefundRecord();
            refundQuery.setOrderNo(order.getOrderNo());
            List<RefundRecord> refunds = refundRecordMapper.selectRefundRecordList(refundQuery);
            if (refunds != null && !refunds.isEmpty()) {
                refunds.sort((a, b) -> {
                    long idA = a.getId() != null ? a.getId() : 0L;
                    long idB = b.getId() != null ? b.getId() : 0L;
                    return Long.compare(idB, idA);
                });
                latestRefund = refunds.get(0);
            }
        } catch (Exception ignored) {
        }

        if (latestRefund != null && latestRefund.getStatus() != null
                && (latestRefund.getStatus() == 1 || latestRefund.getStatus() == 2)) {
            wxStatus = "REFUNDING";
        }
        if (latestRefund != null && latestRefund.getRefundReason() != null) {
            dto.setRefundReason(latestRefund.getRefundReason());
        }
        dto.setStatus(wxStatus);

        List<OrderItem> items = mallOrderService.selectOrderItemListByOrderId(order.getId());
        if (!items.isEmpty()) {
            List<WxOrderDto.Item> dtoItems = new ArrayList<>();
            int totalQuantity = 0;
            for (OrderItem item : items) {
                WxOrderDto.Item dtoItem = new WxOrderDto.Item();
                dtoItem.setProductId(item.getProductId());
                dtoItem.setTitle(item.getProductName());
                dtoItem.setImage(appendListThumb(item.getProductImage()));
                dtoItem.setQuantity(item.getQuantity());
                dtoItem.setPrice(toFen(item.getPrice()));
                dtoItem.setSubtotal(toFen(item.getSubtotal()));
                dtoItems.add(dtoItem);
                totalQuantity += item.getQuantity() != null ? item.getQuantity() : 0;
            }
            dto.setItems(dtoItems);
            dto.setQuantity(totalQuantity > 0 ? totalQuantity : 1);

            OrderItem first = items.get(0);
            dto.setProductId(first.getProductId());
            dto.setTitle(items.size() > 1 ? first.getProductName() + "等" + items.size() + "件商品" : first.getProductName());
            dto.setImage(appendListThumb(first.getProductImage()));
            dto.setPrice(toFen(first.getPrice()));
        }

        if (order.getMerchantId() != null) {
            String merchantName = merchantNameCache.get(order.getMerchantId());
            if (merchantName == null) {
                Merchant merchant = merchantService.selectMerchantById(order.getMerchantId());
                if (merchant != null) {
                    merchantName = merchant.getName();
                    merchantNameCache.put(order.getMerchantId(), merchantName);
                }
            }
            dto.setMerchantName(merchantName);
        }

        if (order.getStatus() != null && order.getStatus() == MallOrderStatus.PENDING && order.getCreateTime() != null) {
            dto.setExpireTime(order.getCreateTime().getTime() + PENDING_EXPIRE_MILLIS);
        }

        if (order.getPayTime() != null) {
            dto.setWriteOffDeadline(order.getPayTime().getTime() + 30L * 24 * 60 * 60 * 1000L);
        }
        dto.setHistory(buildHistory(order.getOrderNo()));

        return dto;
    }

    private List<WxOrderDto.HistoryItem> buildHistory(String orderNo) {
        List<WxOrderDto.HistoryItem> result = new ArrayList<>();
        if (StringUtils.isBlank(orderNo)) {
            return result;
        }
        List<MallOrderStatusHistory> histories = mallOrderService.selectOrderStatusHistory(orderNo);
        if (histories == null) {
            return result;
        }
        for (MallOrderStatusHistory history : histories) {
            WxOrderDto.HistoryItem item = new WxOrderDto.HistoryItem();
            item.setFromStatus(history.getFromStatus());
            item.setToStatus(history.getToStatus());
            item.setStatus(mapDbStatus(history.getToStatus()));
            item.setAction(history.getAction());
            item.setSource(history.getSource());
            item.setOperatorName(history.getOperatorName());
            item.setRemark(history.getRemark());
            item.setChangeTime(history.getChangeTime() != null ? history.getChangeTime().getTime() : null);
            result.add(item);
        }
        return result;
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

    private String mapDbStatus(Integer dbStatus) {
        if (dbStatus == null) {
            return "PENDING_PAY";
        }
        switch (dbStatus) {
            case MallOrderStatus.PENDING:
                return "PENDING_PAY";
            case MallOrderStatus.PAID:
                return "PAID_UNUSED";
            case MallOrderStatus.USED:
            case MallOrderStatus.COMPLETED:
                return "USED_COMPLETED";
            case MallOrderStatus.REFUNDED:
                return "REFUNDED";
            case MallOrderStatus.CANCELLED:
                return "CANCELLED";
            default:
                return "PENDING_PAY";
        }
    }

    private long toFen(BigDecimal amount) {
        return amount != null ? amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact() : 0L;
    }

    private String generateOrderNo() {
        String datePart = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + datePart + randomPart;
    }

    private String generateUniqueWriteOffCode() {
        for (int i = 0; i < MAX_WRITE_OFF_CODE_RETRIES; i++) {
            String code = writeOffCodeGenerator.generate();
            if (mallOrderService.selectOrderByWriteOffCode(code) == null) {
                return code;
            }
        }
        throw new IllegalStateException("write off code collision");
    }
}
