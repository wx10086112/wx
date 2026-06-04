package com.ruoyi.mall.user.mapper;

import com.ruoyi.mall.user.domain.MallUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MallUserMapper {
    MallUser selectMallUserById(Long id);
    MallUser selectMallUserByOpenId(String openId);
    List<MallUser> selectMallUserList(MallUser mallUser);
    int insertMallUser(MallUser mallUser);
    int updateMallUser(MallUser mallUser);
    int deleteMallUserById(Long id);
    int deleteMallUserByIds(Long[] ids);

    @Select("SELECT COUNT(*) FROM mall_user")
    int countTotal();

    @Select("SELECT COUNT(*) FROM mall_user WHERE DATE(create_time) = CURDATE()")
    int countTodayNew();

    @Select({
            "<script>",
            "SELECT COUNT(DISTINCT u.id)",
            "FROM mall_user u",
            "INNER JOIN mall_order o ON o.user_id = u.id AND o.del_flag = '0'",
            "INNER JOIN merchant m ON o.merchant_id = m.id AND m.del_flag = '0'",
            "WHERE 1 = 1",
            "<if test='merchantId != null'>AND o.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "</script>"
    })
    int countTotalScoped(@Param("merchantId") Long merchantId, @Param("distributorId") Long distributorId);

    @Select({
            "<script>",
            "SELECT COUNT(DISTINCT u.id)",
            "FROM mall_user u",
            "INNER JOIN mall_order o ON o.user_id = u.id AND o.del_flag = '0'",
            "INNER JOIN merchant m ON o.merchant_id = m.id AND m.del_flag = '0'",
            "WHERE DATE(u.create_time) = CURDATE()",
            "<if test='merchantId != null'>AND o.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "</script>"
    })
    int countTodayNewScoped(@Param("merchantId") Long merchantId, @Param("distributorId") Long distributorId);
}
