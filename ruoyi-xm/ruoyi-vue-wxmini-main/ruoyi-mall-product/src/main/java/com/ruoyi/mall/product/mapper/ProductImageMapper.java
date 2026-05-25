package com.ruoyi.mall.product.mapper;

import com.ruoyi.mall.product.domain.ProductImage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ProductImageMapper {

    ProductImage selectProductImageById(Long id);

    List<ProductImage> selectProductImageList(ProductImage query);

    List<ProductImage> selectByProductId(Long productId);

    List<ProductImage> selectByProductIdAndType(@Param("productId") Long productId,
                                                  @Param("imageType") String imageType);

    int insertProductImage(ProductImage image);

    int updateProductImage(ProductImage image);

    /** 逻辑删除: status=0 */
    int deleteProductImageById(Long id);

    /** 删除商品所有图片 */
    int deleteByProductId(Long productId);
}
