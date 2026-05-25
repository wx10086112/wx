package com.ruoyi.mall.user.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.mall.user.domain.UserInfo;
import com.ruoyi.mall.user.mapper.UserInfoMapper;
import com.ruoyi.mall.user.service.IUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 用户信息Service业务层处理。优先从缓存读取
 *
 * @author ruoyi
 * @date 2025-04-25
 */
@Service
public class UserInfoServiceImpl implements IUserInfoService {

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private UserInfoMapper userInfoMapper;

    private static final String REDIS_KEY_WX_USER = "wx_user:";

    @Override
    public List<UserInfo> selectUserInfoList(UserInfo userInfo) {
        return userInfoMapper.selectUserInfoList(userInfo);
    }

    @Override
    public int insertUserInfo(UserInfo userInfo) {
        Date now = DateUtils.getNowDate();
        userInfo.setCreateTime(now);
        userInfo.setUpdateTime(now);
        int result = userInfoMapper.insertUserInfo(userInfo);
        redisCache.setCacheObject(REDIS_KEY_WX_USER + userInfo.getUserId(), JSON.toJSONString(userInfo));
        return result;
    }

    @Override
    public int updateUserInfo(UserInfo userInfo) {
        userInfo.setUpdateTime(DateUtils.getNowDate());
        redisCache.setCacheObject(this.getWxUserCacheKey(userInfo.getUserId()), JSON.toJSONString(userInfo));
        return userInfoMapper.updateUserInfo(userInfo);
    }

    @Override
    public UserInfo selectUserInfoByOpenId(String openId) {
        return userInfoMapper.selectUserInfoByOpenId(openId);
    }

    @Override
    public UserInfo selectUserInfoByUserId(String userId) {
        Object t = redisCache.getCacheObject(this.getWxUserCacheKey(userId));
        if (t != null) {
            return JSON.parseObject(t.toString(), UserInfo.class);
        } else {
            UserInfo userInfo = userInfoMapper.selectUserInfoByUserId(userId);
            if (userInfo == null) {
                return null;
            }
            redisCache.setCacheObject(this.getWxUserCacheKey(userId), JSON.toJSONString(userInfo));
            return userInfo;
        }

    }

    private String getWxUserCacheKey(String userId) {
        return REDIS_KEY_WX_USER + userId;
    }
}
