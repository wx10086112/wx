package com.ruoyi.mall.finance.service;

import com.ruoyi.mall.finance.domain.PlatformIncome;

import java.math.BigDecimal;
import java.util.List;

public interface IPlatformIncomeService {

    PlatformIncome selectById(Long id);

    List<PlatformIncome> selectList(PlatformIncome query);

    /**
     * 为订单创建平台收入记录（幂等）
     */
    void createIncome(String orderNo, Long merchantId, BigDecimal orderAmount, BigDecimal commission);

    BigDecimal sumTotalCommission();
}
