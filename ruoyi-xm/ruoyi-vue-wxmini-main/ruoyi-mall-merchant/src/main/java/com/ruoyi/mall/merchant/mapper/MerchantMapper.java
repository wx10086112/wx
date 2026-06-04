package com.ruoyi.mall.merchant.mapper;

import com.ruoyi.mall.merchant.domain.Merchant;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

public interface MerchantMapper {
    Merchant selectMerchantById(Long id);
    Merchant selectMerchantByIdAnyStatus(Long id);
    List<Merchant> selectMerchantList(Merchant merchant);
    int insertMerchant(Merchant merchant);
    int updateMerchant(Merchant merchant);
    int deleteMerchantById(Long id);
    int deleteMerchantByIds(Long[] ids);
    int clearDistributorBindingsByDistributorIds(@Param("ids") Long[] ids);
    int clearRevivedDistributorBindings(@Param("distributorId") Long distributorId);

    @Select("SELECT COUNT(*) FROM merchant WHERE status = 1 AND del_flag = '0'")
    int countActiveMerchant();

    @Select({
            "<script>",
            "SELECT COUNT(*)",
            "FROM merchant",
            "WHERE status = 1 AND del_flag = '0'",
            "<if test='merchantId != null'>AND id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND distributor_id = #{distributorId}</if>",
            "</script>"
    })
    int countActiveMerchantScoped(@Param("merchantId") Long merchantId, @Param("distributorId") Long distributorId);

    @Select("SELECT id, name, total_income as totalIncome FROM merchant WHERE status = 1 AND del_flag = '0' ORDER BY total_income DESC LIMIT #{limit}")
    List<Map> selectMerchantRankByIncome(@Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT id, name, total_income AS totalIncome",
            "FROM merchant",
            "WHERE status = 1 AND del_flag = '0'",
            "<if test='merchantId != null'>AND id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND distributor_id = #{distributorId}</if>",
            "ORDER BY total_income DESC",
            "LIMIT #{limit}",
            "</script>"
    })
    List<Map> selectMerchantRankByIncomeScoped(@Param("limit") int limit,
                                               @Param("merchantId") Long merchantId,
                                               @Param("distributorId") Long distributorId);

    Merchant selectMerchantByCAppId(String cMiniAppId);

    Merchant selectMerchantByMAppId(String mMiniAppId);

    Merchant selectMerchantByAnyMiniAppId(String miniAppId);
}
