package com.ruoyi.mall.product.mapper;

import com.ruoyi.mall.product.domain.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

public interface ProductMapper {

    Product selectProductById(Long id);

    List<Product> selectProductList(Product product);

    List<Product> selectProductByMerchantId(Long merchantId);

    int insertProduct(Product product);

    int updateProduct(Product product);

    int deleteProductById(Long id);

    int deleteProductByIds(Long[] ids);

    int countProductByMerchantId(Long merchantId);

    @Select("SELECT p.*, m.name AS merchant_name FROM product p LEFT JOIN merchant m ON p.merchant_id = m.id WHERE p.status = 1 ORDER BY p.sales DESC LIMIT #{limit}")
    List<Map> selectHotProducts(@Param("limit") int limit);
}
