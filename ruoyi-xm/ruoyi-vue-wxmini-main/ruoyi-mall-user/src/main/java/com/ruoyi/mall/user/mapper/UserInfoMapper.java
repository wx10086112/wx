package com.ruoyi.mall.user.mapper;

import com.ruoyi.mall.user.domain.UserInfo;

import java.util.List;

/**
 * 用户信息Mapper接口
 *
 * @author ruoyi
 * @date 2025-04-25
 */
public interface UserInfoMapper {
    public UserInfo selectUserInfoById(Long id);

    public List<UserInfo> selectUserInfoList(UserInfo userInfo);

    public int insertUserInfo(UserInfo userInfo);

    public int updateUserInfo(UserInfo userInfo);

    public int deleteUserInfoById(Long id);

    public int deleteUserInfoByIds(Long[] ids);

    UserInfo selectUserInfoByOpenId(String openId);

    UserInfo selectUserInfoByUserId(String userId);
}
