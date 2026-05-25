package com.ruoyi.mall.product.service;

import com.ruoyi.mall.product.domain.GrouponActivity;
import com.ruoyi.mall.product.mapper.GrouponActivityMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class GrouponActivityServiceImpl implements IGrouponActivityService {

    @Resource
    private GrouponActivityMapper grouponActivityMapper;

    @Override
    public GrouponActivity selectGrouponActivityById(Long id) {
        return grouponActivityMapper.selectGrouponActivityById(id);
    }

    @Override
    public List<GrouponActivity> selectGrouponActivityList(GrouponActivity query) {
        return grouponActivityMapper.selectGrouponActivityList(query);
    }

    @Override
    public List<GrouponActivity> selectByMerchantId(Long merchantId) {
        return grouponActivityMapper.selectByMerchantId(merchantId);
    }

    @Override
    public List<GrouponActivity> selectActiveActivities() {
        return grouponActivityMapper.selectActiveActivities();
    }

    @Override
    public int insertGrouponActivity(GrouponActivity activity) {
        return grouponActivityMapper.insertGrouponActivity(activity);
    }

    @Override
    public int updateGrouponActivity(GrouponActivity activity) {
        return grouponActivityMapper.updateGrouponActivity(activity);
    }

    @Override
    public int deleteGrouponActivityById(Long id) {
        return grouponActivityMapper.deleteGrouponActivityById(id);
    }

    @Override
    public int deleteGrouponActivityByIds(Long[] ids) {
        return grouponActivityMapper.deleteGrouponActivityByIds(ids);
    }
}
