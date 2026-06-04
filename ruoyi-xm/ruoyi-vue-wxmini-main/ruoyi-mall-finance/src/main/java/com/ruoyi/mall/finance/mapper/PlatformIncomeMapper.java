package com.ruoyi.mall.finance.mapper;

import com.ruoyi.mall.finance.domain.PlatformIncome;
import org.apache.ibatis.annotations.Param;
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

    @Select({"<script>",
            "SELECT COALESCE(SUM(p.commission), 0) FROM platform_income p",
            "LEFT JOIN merchant m ON p.merchant_id = m.id",
            "WHERE p.del_flag = '0'",
            "<if test='merchantId != null'>AND p.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "</script>"})
    BigDecimal sumTotalCommissionScoped(@Param("merchantId") Long merchantId, @Param("distributorId") Long distributorId);
}
