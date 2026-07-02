package com.ruoyi.mall.product.service;

import com.ruoyi.mall.product.domain.Product;
import java.util.List;

public interface IProductService {

    Product selectProductById(Long id);

    List<Product> selectProductList(Product product);

    int insertProduct(Product product);

    int updateProduct(Product product);

    int deleteProductById(Long id);

    int deleteProductByIds(Long[] ids);

    int countProductByMerchantId(Long merchantId);

    Long selectProductVersion(Long merchantId);

    boolean isMerchantAccessibleByDistributor(Long merchantId, Long distributorId);

    boolean isProductAccessibleByDistributor(Long productId, Long distributorId);

    boolean deductStock(Long productId, Integer quantity);

    boolean restoreStock(Long productId, Integer quantity);
}
