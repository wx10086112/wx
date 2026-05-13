package com.ruoyi.wxmini.mapper;

import com.ruoyi.wxmini.domain.MallUser;
import java.util.List;

public interface MallUserMapper {
    MallUser selectMallUserById(Long id);
    MallUser selectMallUserByOpenId(String openId);
    List<MallUser> selectMallUserList(MallUser mallUser);
    int insertMallUser(MallUser mallUser);
    int updateMallUser(MallUser mallUser);
    int deleteMallUserById(Long id);
    int deleteMallUserByIds(Long[] ids);
}
