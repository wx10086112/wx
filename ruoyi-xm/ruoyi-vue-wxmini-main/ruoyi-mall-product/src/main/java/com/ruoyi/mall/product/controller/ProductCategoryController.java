package com.ruoyi.mall.product.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.mall.product.domain.ProductCategory;
import com.ruoyi.mall.product.mapper.ProductCategoryMapper;
import com.ruoyi.mall.product.service.IProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mall/product/category")
public class ProductCategoryController extends BaseController {

    @Autowired
    private ProductCategoryMapper productCategoryMapper;

    @Autowired
    private IProductService productService;

    @PreAuthorize("@ss.hasPermi('mall:product:list')")
    @GetMapping("/list")
    public AjaxResult list(ProductCategory query) {
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null) {
            query.setMerchantId(effMerchantId);
        }
        AjaxResult denied = checkMerchantAccess(query.getMerchantId(), "商品分类");
        if (denied != null) {
            return denied;
        }
        return AjaxResult.success(productCategoryMapper.selectProductCategoryList(query));
    }

    @PreAuthorize("@ss.hasPermi('mall:product:add')")
    @Log(title = "商品分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProductCategory category) {
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null) {
            category.setMerchantId(effMerchantId);
        }
        AjaxResult denied = checkMerchantAccess(category.getMerchantId(), "商品分类");
        if (denied != null) {
            return denied;
        }

        String name = StringUtils.trimToEmpty(category.getName());
        if (StringUtils.isBlank(name)) {
            return AjaxResult.error("分类名称不能为空");
        }
        if (name.length() > 50) {
            return AjaxResult.error("分类名称不能超过50个字符");
        }
        ProductCategory existing = findSameNameCategory(category.getMerchantId(), name);
        if (existing != null) {
            return AjaxResult.success("分类已存在", existing);
        }

        category.setName(name);
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        int rows = productCategoryMapper.insertProductCategory(category);
        return rows > 0 ? AjaxResult.success(category) : AjaxResult.error("新增分类失败");
    }

    private ProductCategory findSameNameCategory(Long merchantId, String name) {
        List<ProductCategory> categories = productCategoryMapper.selectProductCategoryByMerchantId(merchantId);
        for (ProductCategory category : categories) {
            if (StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(category.getName()), name)) {
                return category;
            }
        }
        return null;
    }

    private AjaxResult checkMerchantAccess(Long merchantId, String label) {
        if (merchantId == null) {
            return AjaxResult.error(label + "缺少商家ID");
        }
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null && !effMerchantId.equals(merchantId)) {
            return AjaxResult.error("无权操作该" + label);
        }
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && !productService.isMerchantAccessibleByDistributor(merchantId, effDistributorId)) {
            return AjaxResult.error("无权操作该" + label);
        }
        return null;
    }
}
