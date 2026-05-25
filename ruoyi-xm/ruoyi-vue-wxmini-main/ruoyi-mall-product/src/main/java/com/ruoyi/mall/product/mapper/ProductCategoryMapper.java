package com.ruoyi.mall.product.mapper;

import com.ruoyi.mall.product.domain.ProductCategory;
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
