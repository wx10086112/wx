package com.ruoyi.mall.product.service;

import com.ruoyi.mall.product.domain.GrouponActivity;
import java.util.List;

public interface IGrouponActivityService {

    GrouponActivity selectGrouponActivityById(Long id);

    List<GrouponActivity> selectGrouponActivityList(GrouponActivity query);

    List<GrouponActivity> selectByMerchantId(Long merchantId);

    List<GrouponActivity> selectActiveActivities();

    int insertGrouponActivity(GrouponActivity activity);

    int updateGrouponActivity(GrouponActivity activity);

    int deleteGrouponActivityById(Long id);

    int deleteGrouponActivityByIds(Long[] ids);
}
