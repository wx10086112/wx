package com.ruoyi.mall.product.service;

import com.ruoyi.mall.product.domain.GrouponActivityItem;
import java.util.List;

public interface IGrouponActivityItemService {

    GrouponActivityItem selectGrouponActivityItemById(Long id);

    List<GrouponActivityItem> selectGrouponActivityItemList(GrouponActivityItem query);

    List<GrouponActivityItem> selectByGrouponId(Long grouponId);

    int insertGrouponActivityItem(GrouponActivityItem item);

    int updateGrouponActivityItem(GrouponActivityItem item);

    int deleteGrouponActivityItemById(Long id);

    int deleteGrouponActivityItemByIds(Long[] ids);

    int deleteByGrouponId(Long grouponId);
}
