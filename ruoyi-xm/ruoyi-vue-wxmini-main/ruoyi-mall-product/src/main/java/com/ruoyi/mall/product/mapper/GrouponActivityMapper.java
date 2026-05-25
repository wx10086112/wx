package com.ruoyi.mall.product.mapper;

import com.ruoyi.mall.product.domain.GrouponActivity;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface GrouponActivityMapper {

    GrouponActivity selectGrouponActivityById(Long id);

    List<GrouponActivity> selectGrouponActivityList(GrouponActivity query);

    List<GrouponActivity> selectByMerchantId(@Param("merchantId") Long merchantId);

    List<GrouponActivity> selectActiveActivities();

    int insertGrouponActivity(GrouponActivity activity);

    int updateGrouponActivity(GrouponActivity activity);

    int deleteGrouponActivityById(Long id);

    int deleteGrouponActivityByIds(Long[] ids);
}
