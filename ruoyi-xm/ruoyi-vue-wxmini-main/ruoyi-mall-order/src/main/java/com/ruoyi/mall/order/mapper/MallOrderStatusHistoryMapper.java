package com.ruoyi.mall.order.mapper;

import com.ruoyi.mall.order.domain.MallOrderStatusHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MallOrderStatusHistoryMapper {

    int insertHistory(MallOrderStatusHistory history);

    List<MallOrderStatusHistory> selectByOrderNo(@Param("orderNo") String orderNo);

    List<MallOrderStatusHistory> selectList(MallOrderStatusHistory query);
}
