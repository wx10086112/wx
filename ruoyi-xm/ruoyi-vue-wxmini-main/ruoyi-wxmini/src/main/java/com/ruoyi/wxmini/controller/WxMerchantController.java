package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.domain.MerchantStore;
import com.ruoyi.mall.merchant.mapper.MerchantStoreMapper;
import com.ruoyi.mall.merchant.service.IMerchantService;
import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.service.IProductService;
import com.ruoyi.wxmini.dto.wx.WxGrouponItemDto;
import com.ruoyi.wxmini.dto.wx.WxMerchantItemDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wxmini/merchant")
public class WxMerchantController {

    @Resource
    private IMerchantService merchantService;
    @Resource
    private MerchantStoreMapper merchantStoreMapper;
    @Resource
    private IProductService productService;

    @GetMapping("/home")
    public AjaxResult home(@RequestParam String appid) {
        Merchant merchant = merchantService.selectMerchantByCAppId(appid);
        if (merchant == null || merchant.getStatus() == null || merchant.getStatus() != 1) {
            merchant = merchantService.selectMerchantById(1L);
            if (merchant == null || merchant.getStatus() == null || merchant.getStatus() != 1) {
                return AjaxResult.error("商家不存在或已下线");
            }
        }

        List<MerchantStore> stores = merchantStoreMapper.selectMerchantStoreByMerchantId(merchant.getId());
        MerchantStore mainStore = null;
        for (MerchantStore store : stores) {
            if (store.getIsMain() != null && store.getIsMain() == 1) {
                mainStore = store;
                break;
            }
        }
        if (mainStore == null && !stores.isEmpty()) {
            mainStore = stores.get(0);
        }

        WxMerchantItemDto merchantDto = convertToItemDto(merchant, mainStore, null, null);

        Product query = new Product();
        query.setMerchantId(merchant.getId());
        query.setStatus(1);
        List<Product> products = productService.selectProductList(query);

        List<WxGrouponItemDto> productList = new ArrayList<>();
        for (Product product : products) {
            productList.add(convertProductToDto(product, merchant.getName()));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("merchant", merchantDto);
        result.put("products", productList);
        return AjaxResult.success(result);
    }

    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(required = false) Long categoryId) {
        Merchant query = new Merchant();
        query.setStatus(1);
        List<Merchant> merchants = merchantService.selectMerchantList(query);

        List<WxMerchantItemDto> result = new ArrayList<>();
        for (Merchant merchant : merchants) {
            List<MerchantStore> stores = merchantStoreMapper.selectMerchantStoreByMerchantId(merchant.getId());
            MerchantStore mainStore = null;
            for (MerchantStore store : stores) {
                if (store.getIsMain() != null && store.getIsMain() == 1) {
                    mainStore = store;
                    break;
                }
            }
            if (mainStore == null && !stores.isEmpty()) {
                mainStore = stores.get(0);
            }

            WxMerchantItemDto dto = convertToItemDto(merchant, mainStore, latitude, longitude);
            result.add(dto);
        }
        return AjaxResult.success(result);
    }

    @GetMapping("/detail/{id}")
    public AjaxResult detail(@PathVariable Long id) {
        MerchantStore store = merchantStoreMapper.selectMerchantStoreById(id);
        if (store == null) {
            return AjaxResult.error("门店不存在");
        }
        Merchant merchant = merchantService.selectMerchantById(store.getMerchantId());
        if (merchant == null || merchant.getStatus() == null || merchant.getStatus() != 1) {
            return AjaxResult.error("商家不存在或已下线");
        }
        return AjaxResult.success(convertToItemDto(merchant, store, null, null));
    }

    private WxMerchantItemDto convertToItemDto(Merchant merchant, MerchantStore store,
                                                BigDecimal userLat, BigDecimal userLng) {
        WxMerchantItemDto dto = new WxMerchantItemDto();
        dto.setMerchantId(merchant.getId());
        dto.setName(merchant.getName());
        dto.setShortName(merchant.getName());
        dto.setAvatar(merchant.getLogo());
        dto.setCoverImage(merchant.getLogo());
        dto.setAddress(merchant.getAddress());
        dto.setPhone(merchant.getPhone());
        dto.setBusinessHours(merchant.getBusinessHours());
        dto.setBusinessHoursText(merchant.getBusinessHours() != null ? "周一至周日 " + merchant.getBusinessHours() : null);
        dto.setBusinessStatus(merchant.getStatus() != null && merchant.getStatus() == 1);
        dto.setNotice(merchant.getDescription());
        dto.setIsHot(false);

        if (store != null) {
            dto.setId(store.getId());
            dto.setLatitude(store.getLatitude());
            dto.setLongitude(store.getLongitude());
            if (store.getAddress() != null) {
                dto.setAddress(store.getAddress());
            }
            if (store.getPhone() != null) {
                dto.setPhone(store.getPhone());
            }
            if (store.getBusinessHours() != null) {
                dto.setBusinessHours(store.getBusinessHours());
            }
            if (store.getAvatar() != null) {
                dto.setAvatar(store.getAvatar());
            }

            if (userLat != null && userLng != null
                    && store.getLatitude() != null && store.getLongitude() != null) {
                double distMeters = calculateDistance(
                        userLat.doubleValue(), userLng.doubleValue(),
                        store.getLatitude().doubleValue(), store.getLongitude().doubleValue());
                dto.setDistanceValue((long) distMeters);
                dto.setDistance(distMeters < 1000 ? (int) distMeters + "m"
                        : String.format("%.1fkm", distMeters / 1000));
            }
        }

        int productCount = productService.countProductByMerchantId(merchant.getId());
        dto.setSales(productCount);

        List<String> tags = new ArrayList<>();
        if (merchant.getStatus() != null && merchant.getStatus() == 1) {
            tags.add("营业中");
        }
        dto.setTags(tags);

        List<String> serviceAbilityTags = new ArrayList<>();
        serviceAbilityTags.add("到店核销");
        serviceAbilityTags.add("支持退款");
        dto.setServiceAbilityTags(serviceAbilityTags);

        dto.setFacilityTags(new ArrayList<>());
        dto.setAlbumList(new ArrayList<>());

        return dto;
    }

    private WxGrouponItemDto convertProductToDto(Product product, String merchantName) {
        WxGrouponItemDto dto = new WxGrouponItemDto();
        dto.setId(product.getId());
        dto.setGoodsId(product.getId());
        dto.setTitle(product.getName());
        dto.setSubtitle(product.getDescription());
        dto.setMerchantId(product.getMerchantId());
        dto.setMerchantName(merchantName);
        dto.setImage(product.getCoverImage());
        dto.setPrice(product.getPrice() != null ? product.getPrice().longValue() : 0L);
        dto.setOriginalPrice(product.getOriginalPrice() != null ? product.getOriginalPrice().longValue() : 0L);
        dto.setSales(product.getSales());
        dto.setStock(product.getStock());
        dto.setValidDays(product.getValidDays());
        dto.setStatus(product.getStatus() != null && product.getStatus() == 1 ? "ON_SHELF" : "OFF_SHELF");
        dto.setSort(product.getSort());

        if (product.getValidDays() != null && product.getValidDays() > 0) {
            dto.setValidPeriod("购买后" + product.getValidDays() + "天内有效");
        }

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

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
