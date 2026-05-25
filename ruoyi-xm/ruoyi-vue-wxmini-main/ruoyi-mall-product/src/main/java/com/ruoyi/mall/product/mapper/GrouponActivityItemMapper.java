package com.ruoyi.mall.product.mapper;

import com.ruoyi.mall.product.domain.GrouponActivityItem;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface GrouponActivityItemMapper {

    GrouponActivityItem selectGrouponActivityItemById(Long id);

    List<GrouponActivityItem> selectGrouponActivityItemList(GrouponActivityItem query);

    List<GrouponActivityItem> selectByGrouponId(@Param("grouponId") Long grouponId);

    int insertGrouponActivityItem(GrouponActivityItem item);

    int updateGrouponActivityItem(GrouponActivityItem item);

    int deleteGrouponActivityItemById(Long id);

    int deleteGrouponActivityItemByIds(Long[] ids);

    int deleteByGrouponId(@Param("grouponId") Long grouponId);
}
