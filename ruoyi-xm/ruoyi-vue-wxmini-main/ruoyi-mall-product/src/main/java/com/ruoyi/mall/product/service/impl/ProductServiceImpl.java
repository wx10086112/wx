package com.ruoyi.mall.product.service.impl;

import com.ruoyi.mall.product.domain.Product;
import com.ruoyi.mall.product.mapper.ProductMapper;
import com.ruoyi.mall.product.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Product selectProductById(Long id) {
        return productMapper.selectProductById(id);
    }

    @Override
    public List<Product> selectProductList(Product product) {
        return productMapper.selectProductList(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertProduct(Product product) {
        return productMapper.insertProduct(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProduct(Product product) {
        return productMapper.updateProduct(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProductById(Long id) {
        return productMapper.deleteProductById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProductByIds(Long[] ids) {
        return productMapper.deleteProductByIds(ids);
    }

    @Override
    public int countProductByMerchantId(Long merchantId) {
        return productMapper.countProductByMerchantId(merchantId);
    }

    @Override
    public boolean isMerchantAccessibleByDistributor(Long merchantId, Long distributorId) {
        if (merchantId == null || distributorId == null) {
            return false;
        }
        return productMapper.countMerchantByDistributor(merchantId, distributorId) > 0;
    }

    @Override
    public boolean isProductAccessibleByDistributor(Long productId, Long distributorId) {
        if (productId == null || distributorId == null) {
            return false;
        }
        return productMapper.countProductByDistributor(productId, distributorId) > 0;
    }
}
