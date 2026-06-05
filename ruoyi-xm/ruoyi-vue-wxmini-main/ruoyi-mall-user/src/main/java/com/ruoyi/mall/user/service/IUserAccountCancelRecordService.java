package com.ruoyi.mall.user.service;

import com.ruoyi.mall.user.domain.UserAccountCancelRecord;

import java.util.Date;

public interface IUserAccountCancelRecordService {
    String hashOpenId(String appId, String openId);

    UserAccountCancelRecord selectActiveBlockRecord(String appId, String openIdHash, Date now);

    int saveCancelRecord(UserAccountCancelRecord record);
}
