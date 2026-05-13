package com.ruoyi.wxmini.mapper;

import com.ruoyi.wxmini.domain.WithdrawRecord;
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
}
