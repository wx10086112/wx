package com.ruoyi.wxmini.mapper;

import com.ruoyi.wxmini.domain.Product;
import java.util.List;

public interface ProductMapper {
    Product selectProductById(Long id);
    List<Product> selectProductList(Product product);
    List<Product> selectProductByMerchantId(Long merchantId);
    int insertProduct(Product product);
    int updateProduct(Product product);
    int deleteProductById(Long id);
    int deleteProductByIds(Long[] ids);
    int countProductByMerchantId(Long merchantId);
}
