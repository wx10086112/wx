package com.ruoyi.mall.user.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.mall.user.domain.UserAccountCancelRecord;
import com.ruoyi.mall.user.mapper.UserAccountCancelRecordMapper;
import com.ruoyi.mall.user.service.IUserAccountCancelRecordService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserAccountCancelRecordServiceImpl implements IUserAccountCancelRecordService {

    @Autowired
    private UserAccountCancelRecordMapper recordMapper;

    @Override
    public String hashOpenId(String appId, String openId) {
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(openId)) {
            return "";
        }
        return DigestUtils.sha256Hex(appId + ":" + openId);
    }

    @Override
    public UserAccountCancelRecord selectActiveBlockRecord(String appId, String openIdHash, Date now) {
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(openIdHash)) {
            return null;
        }
        return recordMapper.selectActiveByAppIdAndOpenIdHash(appId, openIdHash, now);
    }

    @Override
    public int saveCancelRecord(UserAccountCancelRecord record) {
        Date now = DateUtils.getNowDate();
        record.setCreateTime(now);
        record.setUpdateTime(now);
        return recordMapper.insertOrUpdateUserAccountCancelRecord(record);
    }
}
