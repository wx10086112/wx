package com.ruoyi.mall.common.service;

import com.ruoyi.mall.common.bo.WxMiniAuthContext;

/**
 * @author weijiayu
 * @date 2025/4/22 22:05
 */
public interface IWxMiniJwtService {

    String createToken(String userId);

    String createToken(WxMiniAuthContext authContext);

    Boolean verifyToken(String token);

    String parseUserId(String token);

    WxMiniAuthContext parseAuthContext(String token);
}
