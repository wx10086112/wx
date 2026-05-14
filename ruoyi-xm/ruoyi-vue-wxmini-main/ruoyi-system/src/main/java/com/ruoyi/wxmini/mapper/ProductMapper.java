package com.ruoyi.wxmini.mapper;

import com.ruoyi.wxmini.domain.Product;
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

    @Select("SELECT p.id, p.name, p.price, p.sales, p.cover_image as coverImage, m.name as merchantName " +
            "FROM product p LEFT JOIN merchant m ON p.merchant_id = m.id " +
            "WHERE p.status = 1 ORDER BY p.sales DESC LIMIT #{limit}")
    List<Map<String, Object>> selectHotProducts(@Param("limit") int limit);
}
