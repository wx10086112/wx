package com.ruoyi.wxmini.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.merchant.domain.Merchant;
import com.ruoyi.mall.merchant.mapper.MerchantMapper;
import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.mapper.ProductMapper;
import com.ruoyi.wxmini.dto.wx.WxGrouponItemDto;
import org.apache.commons.lang3.StringUtils;
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
@RequestMapping("/wxmini/groupon")
public class WxGrouponController {

    @Resource
    private ProductMapper productMapper;
    @Resource
    private MerchantMapper merchantMapper;

    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        Product query = new Product();
        query.setMerchantId(merchantId);
        query.setStatus(1); // 仅上架商品
        List<Product> products = productMapper.selectProductList(query);

        // 关键词过滤
        if (StringUtils.isNotBlank(keyword)) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                if (p.getName() != null && p.getName().contains(keyword)) {
                    filtered.add(p);
                }
            }
            products = filtered;
        }

        // 缓存商家名称
        Map<Long, String> merchantNameCache = new HashMap<>();

        List<WxGrouponItemDto> result = new ArrayList<>();
        for (Product product : products) {
            result.add(convertToDto(product, merchantNameCache));
        }
        return AjaxResult.success(result);
    }

    @GetMapping("/detail/{id}")
    public AjaxResult detail(@PathVariable Long id) {
        Product product = productMapper.selectProductById(id);
        if (product == null) {
            return AjaxResult.error("商品不存在");
        }
        Map<Long, String> merchantNameCache = new HashMap<>();
        return AjaxResult.success(convertToDto(product, merchantNameCache));
    }

    private WxGrouponItemDto convertToDto(Product product, Map<Long, String> merchantNameCache) {
        WxGrouponItemDto dto = new WxGrouponItemDto();
        dto.setId(product.getId());
        dto.setGoodsId(product.getId());
        dto.setTitle(product.getName());
        dto.setSubtitle(product.getDescription());
        dto.setMerchantId(product.getMerchantId());
        dto.setImage(product.getCoverImage());
        dto.setPrice(product.getPrice() != null ? product.getPrice().longValue() : 0L);
        dto.setOriginalPrice(product.getOriginalPrice() != null ? product.getOriginalPrice().longValue() : 0L);
        dto.setSales(product.getSales());
        dto.setStock(product.getStock());
        dto.setValidDays(product.getValidDays());
        dto.setSort(product.getSort());
        dto.setStatus(product.getStatus() != null && product.getStatus() == 1 ? "ON_SHELF" : "OFF_SHELF");

        // 商家名称
        String merchantName = merchantNameCache.get(product.getMerchantId());
        if (merchantName == null && product.getMerchantId() != null) {
            Merchant merchant = merchantMapper.selectMerchantById(product.getMerchantId());
            if (merchant != null) {
                merchantName = merchant.getName();
                merchantNameCache.put(product.getMerchantId(), merchantName);
            }
        }
        dto.setMerchantName(merchantName);

        // 有效期文案
        if (product.getValidDays() != null && product.getValidDays() > 0) {
            dto.setValidPeriod("购买后" + product.getValidDays() + "天内有效");
        }

        // 默认标签
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
}
