package com.ruoyi.mall.finance.mapper;

import com.ruoyi.mall.finance.domain.WithdrawRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

public interface WithdrawRecordMapper {

    WithdrawRecord selectWithdrawRecordById(Long id);

    List<WithdrawRecord> selectWithdrawRecordList(WithdrawRecord withdrawRecord);

    List<WithdrawRecord> selectWithdrawRecordByMerchantId(Long merchantId);

    int insertWithdrawRecord(WithdrawRecord withdrawRecord);

    int updateWithdrawRecord(WithdrawRecord withdrawRecord);

    int deleteWithdrawRecordById(Long id);

    int deleteWithdrawRecordByIds(Long[] ids);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM withdraw_record WHERE status = 2")
    BigDecimal sumPaidTotal();

    @Select({"<script>",
            "SELECT COALESCE(SUM(w.amount), 0) FROM withdraw_record w",
            "LEFT JOIN merchant m ON w.merchant_id = m.id",
            "WHERE w.del_flag = '0' AND w.status = 2",
            "<if test='merchantId != null'>AND w.merchant_id = #{merchantId}</if>",
            "<if test='distributorId != null'>AND m.distributor_id = #{distributorId}</if>",
            "</script>"})
    BigDecimal sumPaidTotalScoped(@Param("merchantId") Long merchantId, @Param("distributorId") Long distributorId);
}
