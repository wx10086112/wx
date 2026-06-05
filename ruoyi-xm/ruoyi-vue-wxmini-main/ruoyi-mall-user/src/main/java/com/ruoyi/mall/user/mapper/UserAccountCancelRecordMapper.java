package com.ruoyi.mall.user.mapper;

import com.ruoyi.mall.user.domain.UserAccountCancelRecord;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface UserAccountCancelRecordMapper {
    UserAccountCancelRecord selectActiveByAppIdAndOpenIdHash(@Param("appId") String appId,
                                                             @Param("openIdHash") String openIdHash,
                                                             @Param("now") Date now);

    int insertOrUpdateUserAccountCancelRecord(UserAccountCancelRecord record);
}
