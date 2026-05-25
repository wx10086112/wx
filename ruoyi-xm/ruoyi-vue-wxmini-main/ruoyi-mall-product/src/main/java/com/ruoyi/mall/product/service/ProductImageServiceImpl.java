package com.ruoyi.mall.product.service;

import com.ruoyi.mall.product.domain.ProductImage;
import com.ruoyi.mall.product.mapper.ProductImageMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ProductImageServiceImpl implements IProductImageService {

    @Resource
    private ProductImageMapper productImageMapper;

    @Override
    public ProductImage selectProductImageById(Long id) {
        return productImageMapper.selectProductImageById(id);
    }

    @Override
    public List<ProductImage> selectProductImageList(ProductImage query) {
        return productImageMapper.selectProductImageList(query);
    }

    @Override
    public List<ProductImage> selectByProductId(Long productId) {
        return productImageMapper.selectByProductId(productId);
    }

    @Override
    public List<ProductImage> selectByProductIdAndType(Long productId, String imageType) {
        return productImageMapper.selectByProductIdAndType(productId, imageType);
    }

    @Override
    public int insertProductImage(ProductImage image) {
        if (image.getStatus() == null) {
            image.setStatus(1);
        }
        return productImageMapper.insertProductImage(image);
    }

    @Override
    public int updateProductImage(ProductImage image) {
        return productImageMapper.updateProductImage(image);
    }

    @Override
    public int deleteProductImageById(Long id) {
        return productImageMapper.deleteProductImageById(id);
    }

    @Override
    public int deleteByProductId(Long productId) {
        return productImageMapper.deleteByProductId(productId);
    }
}
