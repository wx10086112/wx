package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.order.domain.BookingService;
import com.ruoyi.mall.order.mapper.BookingServiceMapper;
import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.service.IProductService;
import com.ruoyi.wxmini.dto.wx.WxGrouponItemDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wxmini/groupon")
public class WxGrouponController {

    @Resource
    private IProductService productService;
    @Resource
    private IMerchantService merchantService;
    @Resource
    private BookingServiceMapper bookingServiceMapper;

    @GetMapping("/version")
    public AjaxResult version(@RequestParam(required = false) Long merchantId) {
        Long currentMerchantId = WxMiniUserContext.getCurrentMerchantId();
        if (currentMerchantId == null) {
            currentMerchantId = WxMiniUserContext.getAppIdMerchantId();
        }
        Long targetMerchantId = currentMerchantId != null ? currentMerchantId : merchantId;
        Long productVersion = productService.selectProductVersion(targetMerchantId);
        Map<String, Object> data = new HashMap<>();
        data.put("merchantId", targetMerchantId);
        long productVersionValue = productVersion != null ? productVersion : 0L;
        long bookingServiceVersion = selectBookingServiceVersionSafely(targetMerchantId);
        data.put("version", productVersionValue * 1_000_003L + bookingServiceVersion);
        return AjaxResult.success(data);
    }

    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        // SaaS数据隔离：强制限定为当前商家
        Long currentMerchantId = WxMiniUserContext.getCurrentMerchantId();
        if (currentMerchantId == null) {
            currentMerchantId = WxMiniUserContext.getAppIdMerchantId();
        }
        Product query = new Product();
        query.setMerchantId(currentMerchantId != null ? currentMerchantId : merchantId);
        query.setStatus(1);

        syncActiveProductBookingServicesSafely(query.getMerchantId());
        List<BookingService> services = selectBookingServicesSafely(query.getMerchantId(), keyword);
        if (!services.isEmpty()) {
            List<WxGrouponItemDto> result = new ArrayList<>();
            Map<Long, String> merchantNameCache = new HashMap<>();
            for (BookingService service : services) {
                if (currentMerchantId == null) {
                    Merchant merchant = merchantService.selectMerchantById(service.getMerchantId());
                    if (merchant == null || !merchant.canOperate()) {
                        continue;
                    }
                }
                result.add(convertServiceToDto(service, merchantNameCache, true));
            }
            return AjaxResult.success(result);
        }

        List<Product> products = productService.selectProductList(query);

        // 过滤未满足运营准入条件的商户商品
        if (currentMerchantId == null) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                Merchant merchant = merchantService.selectMerchantById(p.getMerchantId());
                if (merchant != null && merchant.canOperate()) {
                    filtered.add(p);
                }
            }
            products = filtered;
        }

        if (StringUtils.isNotBlank(keyword)) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                if (p.getName() != null && p.getName().contains(keyword)) {
                    filtered.add(p);
                }
            }
            products = filtered;
        }

        Map<Long, String> merchantNameCache = new HashMap<>();

        List<WxGrouponItemDto> result = new ArrayList<>();
        for (Product product : products) {
            result.add(convertToDto(product, merchantNameCache, true));
        }
        return AjaxResult.success(result);
    }

    @GetMapping("/detail/{id}")
    public AjaxResult detail(@PathVariable Long id) {
        BookingService service = selectBookingServiceByEntryIdSafely(id);
        if (service != null) {
            if (!isActiveBookingService(service)) {
                return AjaxResult.error("鏈嶅姟涓嶅瓨鍦?");
            }
            Long currentMerchantId = WxMiniUserContext.getCurrentMerchantId();
            if (currentMerchantId == null) {
                currentMerchantId = WxMiniUserContext.getAppIdMerchantId();
            }
            if (currentMerchantId != null && !currentMerchantId.equals(service.getMerchantId())) {
                return AjaxResult.error("服务不存在");
            }
            Merchant merchant = merchantService.selectMerchantById(service.getMerchantId());
            if (merchant == null || !merchant.canOperate()) {
                return AjaxResult.error("服务不存在");
            }
            Map<Long, String> merchantNameCache = new HashMap<>();
            return AjaxResult.success(convertServiceToDto(service, merchantNameCache, false));
        }

        Product product = productService.selectProductById(id);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            return AjaxResult.error("商品不存在");
        }
        // 校验商品属于当前商家
        Long currentMerchantId = WxMiniUserContext.getCurrentMerchantId();
        if (currentMerchantId == null) {
            currentMerchantId = WxMiniUserContext.getAppIdMerchantId();
        }
        if (currentMerchantId != null && !currentMerchantId.equals(product.getMerchantId())) {
            return AjaxResult.error("商品不存在");
        }
        // 校验商家运营准入
        Merchant merchant = merchantService.selectMerchantById(product.getMerchantId());
        if (merchant == null || !merchant.canOperate()) {
            return AjaxResult.error("商品不存在");
        }
        Map<Long, String> merchantNameCache = new HashMap<>();
        return AjaxResult.success(convertToDto(product, merchantNameCache, false));
    }

    private void syncActiveProductBookingServicesSafely(Long merchantId) {
        try {
            bookingServiceMapper.syncActiveProductBookingServices(merchantId);
        } catch (Exception ignored) {
        }
    }

    private List<BookingService> selectBookingServicesSafely(Long merchantId, String keyword) {
        try {
            List<BookingService> services = bookingServiceMapper.selectActiveBookingServiceList(merchantId, keyword);
            return services != null ? services : new ArrayList<>();
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private BookingService selectBookingServiceByEntryIdSafely(Long id) {
        if (id == null) {
            return null;
        }
        try {
            BookingService service = bookingServiceMapper.selectBookingServiceById(id);
            if (service != null) {
                return service;
            }
            return bookingServiceMapper.selectBookingServiceByProductId(id);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isActiveBookingService(BookingService service) {
        return service != null
                && service.getStatus() != null
                && service.getStatus() == 1
                && isLinkedProductAvailable(service);
    }

    private boolean isLinkedProductAvailable(BookingService service) {
        if (service == null || service.getProductId() == null) {
            return true;
        }
        Product product = productService.selectProductById(service.getProductId());
        return product != null
                && service.getMerchantId() != null
                && service.getMerchantId().equals(product.getMerchantId())
                && product.getStatus() != null
                && product.getStatus() == 1;
    }

    private Long selectBookingServiceVersionSafely(Long merchantId) {
        try {
            Long version = bookingServiceMapper.selectBookingServiceVersion(merchantId);
            return version != null ? version : 0L;
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private WxGrouponItemDto convertServiceToDto(BookingService service, Map<Long, String> merchantNameCache, boolean listThumb) {
        WxGrouponItemDto dto = new WxGrouponItemDto();
        Long entryId = service.getId();
        dto.setId(entryId);
        dto.setGoodsId(entryId);
        dto.setProductId(service.getProductId() != null ? service.getProductId() : entryId);
        dto.setTitle(service.getServiceName());
        dto.setSubtitle(service.getDescription());
        dto.setDescription(service.getDescription());
        dto.setMerchantId(service.getMerchantId());
        dto.setImage(appendThumb(service.getServiceImage(), listThumb ? "list" : "detail"));
        dto.setPrice(toFen(service.getServicePrice()));
        dto.setOriginalPrice(toFen(service.getOriginalPrice()));
        dto.setSales(0);
        dto.setTotalSales(0);
        dto.setStock(service.getStock() != null ? service.getStock() : 999999);
        dto.setSort(service.getSort());
        dto.setStatus(service.getStatus() != null && service.getStatus() == 1 ? "ON_SHELF" : "OFF_SHELF");

        String merchantName = merchantNameCache.get(service.getMerchantId());
        if (merchantName == null && service.getMerchantId() != null) {
            Merchant merchant = merchantService.selectMerchantById(service.getMerchantId());
            if (merchant != null) {
                merchantName = merchant.getName();
                merchantNameCache.put(service.getMerchantId(), merchantName);
            }
        }
        dto.setMerchantName(merchantName);

        List<String> tags = new ArrayList<>();
        tags.add("到店服务");
        tags.add("可预点单");
        dto.setTags(tags);
        dto.setContentDetail(new ArrayList<>());
        dto.setBookingRequired(true);
        dto.setBookingRule("提交预点单后等待门店确认");
        dto.setRefundRule("");
        dto.setLimitRule("");
        return dto;
    }

    private WxGrouponItemDto convertToDto(Product product, Map<Long, String> merchantNameCache, boolean listThumb) {
        WxGrouponItemDto dto = new WxGrouponItemDto();
        dto.setId(product.getId());
        dto.setGoodsId(product.getId());
        dto.setProductId(product.getId());
        dto.setTitle(product.getName());
        dto.setSubtitle(product.getDescription());
        dto.setMerchantId(product.getMerchantId());
        dto.setImage(appendThumb(product.getCoverImage(), listThumb ? "list" : "detail"));
        dto.setPrice(toFen(product.getPrice()));
        dto.setOriginalPrice(toFen(product.getOriginalPrice()));
        dto.setSales(product.getSales());
        dto.setTotalSales(product.getSales());
        dto.setStock(product.getStock());
        dto.setValidDays(product.getValidDays());
        dto.setSort(product.getSort());
        dto.setStatus(product.getStatus() != null && product.getStatus() == 1 ? "ON_SHELF" : "OFF_SHELF");

        String merchantName = merchantNameCache.get(product.getMerchantId());
        if (merchantName == null && product.getMerchantId() != null) {
            Merchant merchant = merchantService.selectMerchantById(product.getMerchantId());
            if (merchant != null) {
                merchantName = merchant.getName();
                merchantNameCache.put(product.getMerchantId(), merchantName);
            }
        }
        dto.setMerchantName(merchantName);

        if (product.getValidDays() != null && product.getValidDays() > 0) {
            dto.setValidPeriod("购买后" + product.getValidDays() + "天内有效");
        }
        dto.setVerifyNotice(product.getVerifyNotice());

        List<String> tags = new ArrayList<>();
        if (product.getSales() != null && product.getSales() > 100) {
            tags.add("热销");
        }
        tags.add("到店使用");
        dto.setTags(tags);

        dto.setContentDetail(new ArrayList<>());
        dto.setBookingRequired(false);
        dto.setRefundRule("过期自动退款");
        dto.setLimitRule("");

        return dto;
    }

    private String appendThumb(String imageUrl, String thumb) {
        if (StringUtils.isBlank(imageUrl)
                || (!imageUrl.contains("/profile/merchant_images/") && !imageUrl.contains("/profile/merchant-goods/"))
                || imageUrl.contains("?thumb=")
                || imageUrl.contains("&thumb=")) {
            return imageUrl;
        }
        return imageUrl + (imageUrl.contains("?") ? "&" : "?") + "thumb=" + thumb;
    }

    private long toFen(BigDecimal amount) {
        return amount != null ? amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact() : 0L;
    }
}
