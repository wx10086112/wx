package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.util.WriteOffCodeGenerator;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.OrderItem;
import com.ruoyi.mall.order.service.IMallOrderService;
import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.service.IProductService;
import com.ruoyi.wxmini.dto.wx.WxOrderCreateRequestDto;
import com.ruoyi.wxmini.dto.wx.WxOrderDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/wxmini/order")
public class WxOrderController {

    private static final int ORDER_STATUS_PENDING = 0;
    private static final int ORDER_STATUS_PAID = 1;
    private static final int ORDER_STATUS_USED = 2;
    private static final int ORDER_STATUS_COMPLETED = 3;
    private static final int ORDER_STATUS_REFUNDED = 4;
    private static final int ORDER_STATUS_CANCELLED = 5;

    @Resource
    private IMallOrderService mallOrderService;
    @Resource
    private IProductService productService;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private WriteOffCodeGenerator writeOffCodeGenerator;

    @PostMapping("/create")
    public AjaxResult create(@Valid @RequestBody WxOrderCreateRequestDto requestDto) {
        if (requestDto == null) {
            return AjaxResult.error("请求参数不能为空");
        }

        Long userId = Long.valueOf(WxMiniUserContext.getCurrentUserId());

        List<WxOrderCreateRequestDto.OrderItemInput> itemInputs = new ArrayList<>();
        if (requestDto.getItems() != null && !requestDto.getItems().isEmpty()) {
            itemInputs = requestDto.getItems();
        } else if (requestDto.getProductId() != null) {
            WxOrderCreateRequestDto.OrderItemInput single = new WxOrderCreateRequestDto.OrderItemInput();
            single.setProductId(requestDto.getProductId());
            single.setQuantity(requestDto.getQuantity() != null ? requestDto.getQuantity() : 1);
            itemInputs.add(single);
        }
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
            int qty = input.getQuantity() != null && input.getQuantity() > 0 ? input.getQuantity() : 1;
            BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(price.multiply(new BigDecimal(qty)));
            products.add(product);
        }

        // 校验商品商家属于当前C端上下文
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
        String writeOffCode = generateWriteOffCode();

        MallOrder order = new MallOrder();
        order.setOrderNo(orderNo);
        order.setMerchantId(merchantId);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setCouponAmount(couponAmount);
        order.setCouponId(requestDto.getCouponId());
        order.setStatus(ORDER_STATUS_PENDING);
        order.setWriteOffCode(writeOffCode);
        mallOrderService.insertMallOrder(order);

        for (int i = 0; i < itemInputs.size(); i++) {
            WxOrderCreateRequestDto.OrderItemInput input = itemInputs.get(i);
            Product product = products.get(i);
            int qty = input.getQuantity() != null && input.getQuantity() > 0 ? input.getQuantity() : 1;
            BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setOrderNo(orderNo);
            item.setMerchantId(merchantId);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getCoverImage());
            item.setPrice(price);
            item.setQuantity(qty);
            item.setSubtotal(price.multiply(new BigDecimal(qty)));
            mallOrderService.insertOrderItem(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("orderAmount", totalAmount.longValue());
        result.put("couponAmount", couponAmount.longValue());
        result.put("payAmount", payAmount.longValue());
        return AjaxResult.success(result);
    }

    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String status) {
        Long userId = Long.valueOf(WxMiniUserContext.getCurrentUserId());

        MallOrder query = new MallOrder();
        query.setUserId(userId);
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
        Long userId = Long.valueOf(WxMiniUserContext.getCurrentUserId());
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return AjaxResult.error("订单不存在");
        }
        return AjaxResult.success(convertToDto(order, new HashMap<>()));
    }

    @PostMapping("/cancel/{orderNo}")
    public AjaxResult cancel(@PathVariable String orderNo) {
        Long userId = Long.valueOf(WxMiniUserContext.getCurrentUserId());
        MallOrder order = mallOrderService.selectMallOrderByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return AjaxResult.error("订单不存在");
        }
        if (order.getStatus() != ORDER_STATUS_PENDING) {
            return AjaxResult.error("当前订单状态不可取消");
        }

        MallOrder update = new MallOrder();
        update.setId(order.getId());
        update.setStatus(ORDER_STATUS_CANCELLED);
        update.setCancelTime(new Date());
        mallOrderService.updateMallOrder(update);

        order.setStatus(ORDER_STATUS_CANCELLED);
        order.setCancelTime(new Date());
        return AjaxResult.success(convertToDto(order, new HashMap<>()));
    }

    private WxOrderDto convertToDto(MallOrder order, Map<Long, String> merchantNameCache) {
        WxOrderDto dto = new WxOrderDto();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setMerchantId(order.getMerchantId());
        dto.setStatus(mapDbStatus(order.getStatus()));
        dto.setWriteOffCode(order.getWriteOffCode());
        dto.setOrderAmount(order.getTotalAmount() != null ? order.getTotalAmount().longValue() : 0L);
        dto.setCouponAmount(order.getCouponAmount() != null ? order.getCouponAmount().longValue() : 0L);
        dto.setPayAmount(order.getPayAmount() != null ? order.getPayAmount().longValue() : 0L);
        dto.setCreateTime(order.getCreateTime() != null ? order.getCreateTime().getTime() : 0L);
        dto.setPayTime(order.getPayTime() != null ? order.getPayTime().getTime() : null);
        dto.setWriteOffTime(order.getUseTime() != null ? order.getUseTime().getTime() : null);
        dto.setRefundTime(order.getRefundTime() != null ? order.getRefundTime().getTime() : null);
        dto.setQuantity(1);

        List<OrderItem> items = mallOrderService.selectOrderItemListByOrderId(order.getId());
        if (!items.isEmpty()) {
            OrderItem first = items.get(0);
            dto.setProductId(first.getProductId());
            dto.setTitle(first.getProductName());
            dto.setImage(first.getProductImage());
            dto.setPrice(first.getPrice() != null ? first.getPrice().longValue() : 0L);
            dto.setQuantity(first.getQuantity());
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

        if (order.getStatus() == ORDER_STATUS_PENDING && order.getCreateTime() != null) {
            dto.setExpireTime(order.getCreateTime().getTime() + 15 * 60 * 1000L);
        }

        if (order.getPayTime() != null) {
            dto.setWriteOffDeadline(order.getPayTime().getTime() + 30L * 24 * 60 * 60 * 1000L);
        }

        return dto;
    }

    private String mapDbStatus(Integer dbStatus) {
        if (dbStatus == null) return "PENDING_PAY";
        switch (dbStatus) {
            case ORDER_STATUS_PENDING: return "PENDING_PAY";
            case ORDER_STATUS_PAID:
            case ORDER_STATUS_USED: return "PAID_UNUSED";
            case ORDER_STATUS_COMPLETED: return "USED_COMPLETED";
            case ORDER_STATUS_REFUNDED: return "REFUNDED";
            case ORDER_STATUS_CANCELLED: return "CANCELLED";
            default: return "PENDING_PAY";
        }
    }

    private String generateOrderNo() {
        String datePart = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + datePart + randomPart;
    }

    private String generateWriteOffCode() {
        return writeOffCodeGenerator.generate();
    }
}
