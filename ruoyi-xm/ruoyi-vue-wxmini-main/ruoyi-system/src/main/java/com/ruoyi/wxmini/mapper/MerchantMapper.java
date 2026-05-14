package com.ruoyi.wxmini.mapper;

import com.ruoyi.wxmini.domain.Merchant;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

public interface MerchantMapper {
    Merchant selectMerchantById(Long id);
    List<Merchant> selectMerchantList(Merchant merchant);
    int insertMerchant(Merchant merchant);
    int updateMerchant(Merchant merchant);
    int deleteMerchantById(Long id);
    int deleteMerchantByIds(Long[] ids);

    @Select("SELECT COUNT(*) FROM merchant WHERE status = 1")
    int countActiveMerchant();

    @Select("SELECT id, name, total_income as totalIncome, total_sales as totalSales, total_orders as totalOrders " +
            "FROM merchant WHERE status = 1 ORDER BY total_income DESC LIMIT #{limit}")
    List<Map<String, Object>> selectMerchantRankByIncome(@Param("limit") int limit);
}
