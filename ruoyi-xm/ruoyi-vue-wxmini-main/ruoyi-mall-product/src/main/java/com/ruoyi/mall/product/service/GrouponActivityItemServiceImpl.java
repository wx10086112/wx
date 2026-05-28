package com.ruoyi.mall.product.service;

import com.ruoyi.mall.product.domain.GrouponActivityItem;
import com.ruoyi.mall.product.mapper.GrouponActivityItemMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class GrouponActivityItemServiceImpl implements IGrouponActivityItemService {

    @Resource
    private GrouponActivityItemMapper grouponActivityItemMapper;

    @Override
    public GrouponActivityItem selectGrouponActivityItemById(Long id) {
        return grouponActivityItemMapper.selectGrouponActivityItemById(id);
    }

    @Override
    public List<GrouponActivityItem> selectGrouponActivityItemList(GrouponActivityItem query) {
        return grouponActivityItemMapper.selectGrouponActivityItemList(query);
    }

    @Override
    public List<GrouponActivityItem> selectByGrouponId(Long grouponId) {
        return grouponActivityItemMapper.selectByGrouponId(grouponId);
    }

    @Override
    public int insertGrouponActivityItem(GrouponActivityItem item) {
        // 自动计算折扣
        calculateDiscount(item);
        return grouponActivityItemMapper.insertGrouponActivityItem(item);
    }

    @Override
    public int updateGrouponActivityItem(GrouponActivityItem item) {
        calculateDiscount(item);
        return grouponActivityItemMapper.updateGrouponActivityItem(item);
    }

    @Override
    public int deleteGrouponActivityItemById(Long id) {
        return grouponActivityItemMapper.deleteGrouponActivityItemById(id);
    }

    @Override
    public int deleteGrouponActivityItemByIds(Long[] ids) {
        return grouponActivityItemMapper.deleteGrouponActivityItemByIds(ids);
    }

    @Override
    public int deleteByGrouponId(Long grouponId) {
        return grouponActivityItemMapper.deleteByGrouponId(grouponId);
    }

    private void calculateDiscount(GrouponActivityItem item) {
        if (item.getOriginalPrice() != null && item.getGrouponPrice() != null
                && item.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0
                && item.getGrouponPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal original = item.getOriginalPrice();
            BigDecimal groupon = item.getGrouponPrice();
            // 折扣 = 团购价 / 原价 * 10
            BigDecimal rate = groupon.divide(original, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.TEN);
            item.setDiscountRate(rate);
        }
    }
}
