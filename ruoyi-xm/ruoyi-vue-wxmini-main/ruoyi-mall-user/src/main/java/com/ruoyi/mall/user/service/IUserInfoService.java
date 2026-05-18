package com.ruoyi.mall.user.service;

import com.ruoyi.mall.user.domain.UserInfo;

import java.util.List;

/**
 * 用户信息Service接口
 *
 * @author ruoyi
 * @date 2025-04-25
 */
public interface IUserInfoService {

    public List<UserInfo> selectUserInfoList(UserInfo userInfo);

    public int insertUserInfo(UserInfo userInfo);

    public int updateUserInfo(UserInfo userInfo);

    UserInfo selectUserInfoByOpenId(String openId);

    UserInfo selectUserInfoByUserId(String userId);
}
