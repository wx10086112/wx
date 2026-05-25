package com.ruoyi.mall.product.service;

import com.ruoyi.mall.product.domain.ProductImage;
import java.util.List;

public interface IProductImageService {

    ProductImage selectProductImageById(Long id);

    List<ProductImage> selectProductImageList(ProductImage query);

    List<ProductImage> selectByProductId(Long productId);

    List<ProductImage> selectByProductIdAndType(Long productId, String imageType);

    int insertProductImage(ProductImage image);

    int updateProductImage(ProductImage image);

    int deleteProductImageById(Long id);

    int deleteByProductId(Long productId);
}
