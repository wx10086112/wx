package com.ruoyi.wxmini.mapper;

import com.ruoyi.wxmini.domain.PlatformIncome;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.util.List;

public interface PlatformIncomeMapper {
    PlatformIncome selectPlatformIncomeById(Long id);
    List<PlatformIncome> selectPlatformIncomeList(PlatformIncome platformIncome);
    List<PlatformIncome> selectPlatformIncomeByMerchantId(Long merchantId);
    int insertPlatformIncome(PlatformIncome platformIncome);
    int updatePlatformIncome(PlatformIncome platformIncome);
    int deletePlatformIncomeById(Long id);
    int deletePlatformIncomeByIds(Long[] ids);

    @Select("SELECT COALESCE(SUM(commission), 0) FROM platform_income")
    BigDecimal sumTotalCommission();
}
