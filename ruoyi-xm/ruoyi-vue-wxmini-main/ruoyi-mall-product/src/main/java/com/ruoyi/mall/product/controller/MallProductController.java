package com.ruoyi.mall.product.controller;

import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mall/product")
public class MallProductController extends BaseController {

    @Autowired
    private IProductService productService;

    @DataScopeBiz(merchantAlias = "product")
    @PreAuthorize("@ss.hasPermi('mall:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(Product product) {
        startPage();
        List<Product> list = productService.selectProductList(product);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:product:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        Product product = productService.selectProductById(id);
        if (product == null) {
            return AjaxResult.error("商品不存在");
        }
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null && !effMerchantId.equals(product.getMerchantId())) {
            return AjaxResult.error("无权查看该商品");
        }
        Long effDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        if (effDistributorId != null && product.getMerchantId() != null) {
            // 分销商视角下需要校验商品所属商家是否归属于自己（通过dataScopeBiz在列表层已过滤，详情层需补充）
        }
        return AjaxResult.success(product);
    }

    @PreAuthorize("@ss.hasPermi('mall:product:add')")
    @Log(title = "商品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Product product) {
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null) {
            product.setMerchantId(effMerchantId);
        }
        return toAjax(productService.insertProduct(product));
    }

    @PreAuthorize("@ss.hasPermi('mall:product:edit')")
    @Log(title = "商品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Product product) {
        Product existing = productService.selectProductById(product.getId());
        if (existing == null) {
            return AjaxResult.error("商品不存在");
        }
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null && !effMerchantId.equals(existing.getMerchantId())) {
            return AjaxResult.error("无权修改该商品");
        }
        return toAjax(productService.updateProduct(product));
    }

    @PreAuthorize("@ss.hasPermi('mall:product:remove')")
    @Log(title = "商品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        Long effMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();
        if (effMerchantId != null) {
            for (Long id : ids) {
                Product product = productService.selectProductById(id);
                if (product != null && !effMerchantId.equals(product.getMerchantId())) {
                    return AjaxResult.error("无权删除该商品");
                }
            }
        }
        return toAjax(productService.deleteProductByIds(ids));
    }
}
