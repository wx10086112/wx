package com.ruoyi.wxmini.mapper;

import com.ruoyi.wxmini.domain.ProductCategory;
import java.util.List;

public interface ProductCategoryMapper {
    ProductCategory selectProductCategoryById(Long id);
    List<ProductCategory> selectProductCategoryList(ProductCategory productCategory);
    List<ProductCategory> selectProductCategoryByMerchantId(Long merchantId);
    int insertProductCategory(ProductCategory productCategory);
    int updateProductCategory(ProductCategory productCategory);
    int deleteProductCategoryById(Long id);
    int deleteProductCategoryByIds(Long[] ids);
}
