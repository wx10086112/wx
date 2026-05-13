package com.ruoyi.wxmini.service;

import com.ruoyi.wxmini.domain.Product;
import java.util.List;

public interface IProductService {
    Product selectProductById(Long id);
    List<Product> selectProductList(Product product);
    int insertProduct(Product product);
    int updateProduct(Product product);
    int deleteProductById(Long id);
    int deleteProductByIds(Long[] ids);
}
