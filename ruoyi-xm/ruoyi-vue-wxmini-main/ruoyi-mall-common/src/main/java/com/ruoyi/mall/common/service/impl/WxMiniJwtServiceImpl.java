package com.ruoyi.mall.common.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.ruoyi.mall.common.bo.WxMiniAuthContext;
import com.ruoyi.mall.common.service.IWxMiniJwtService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author weijiayu
 * @date 2025/4/22 22:09
 */
@Service
@Slf4j
public class WxMiniJwtServiceImpl implements IWxMiniJwtService {

    private static final String JWT_KEY_USER_ID = "userId";

    private static final String JWT_KEY_USER_TYPE = "userType";

    private static final String JWT_KEY_STAFF_ID = "staffId";

    private static final String JWT_KEY_MERCHANT_ID = "merchantId";

    private static final String JWT_KEY_STORE_ID = "storeId";

    private static final String JWT_KEY_ROLE_CODES = "roleCodes";

    private static final String JWT_KEY_PERMISSION_CODES = "permissionCodes";

    // jwt密钥
    @Value("${token.secret}")
    private String key;
    // jwt有效期。单位分钟
    @Value("${token.expireTime:60}")
    private int expireTime;

    @PostConstruct
    public void checkSecret() {
        if (StringUtils.isBlank(key) || "asd".equals(key) || "test".equals(key) || "123456".equals(key) || key.length() < 32) {
            throw new IllegalStateException("token.secret 未配置或强度不足（至少32位随机字符串）");
        }
    }

    @Override
    public String createToken(String userId) {
        WxMiniAuthContext authContext = new WxMiniAuthContext();
        authContext.setUserId(userId);
        authContext.setUserType(WxMiniAuthContext.USER_TYPE_WX_USER);
        return createToken(authContext);
    }

    @Override
    public String createToken(WxMiniAuthContext authContext) {
        if (authContext == null || StringUtils.isBlank(authContext.getUserId())) {
            throw new IllegalArgumentException("用户身份不能为空");
        }
        if (StringUtils.isBlank(authContext.getUserType())) {
            authContext.setUserType(WxMiniAuthContext.USER_TYPE_WX_USER);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put(JWT_KEY_USER_ID, authContext.getUserId());
        payload.put(JWT_KEY_USER_TYPE, authContext.getUserType());
        if (authContext.getStaffId() != null) {
            payload.put(JWT_KEY_STAFF_ID, authContext.getStaffId());
        }
        if (authContext.getMerchantId() != null) {
            payload.put(JWT_KEY_MERCHANT_ID, authContext.getMerchantId());
        }
        if (authContext.getStoreId() != null) {
            payload.put(JWT_KEY_STORE_ID, authContext.getStoreId());
        }
        payload.put(JWT_KEY_ROLE_CODES, joinList(authContext.getRoleCodes()));
        payload.put(JWT_KEY_PERMISSION_CODES, joinList(authContext.getPermissionCodes()));
        payload.put(JWTPayload.EXPIRES_AT, DateTime.now().offset(DateField.MINUTE, expireTime));
        return JWTUtil.createToken(payload, key.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Boolean verifyToken(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        jwt.setKey(key.getBytes(StandardCharsets.UTF_8));
        // 校验签名和有效期
        return jwt.verify() && jwt.validate(0);
    }

    @Override
    public String parseUserId(String token) {
        WxMiniAuthContext authContext = parseAuthContext(token);
        return authContext.getUserId();
    }

    @Override
    public WxMiniAuthContext parseAuthContext(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        WxMiniAuthContext authContext = new WxMiniAuthContext();
        authContext.setUserId(parseString(jwt.getPayload(JWT_KEY_USER_ID)));
        authContext.setUserType(parseString(jwt.getPayload(JWT_KEY_USER_TYPE)));
        authContext.setStaffId(parseLong(jwt.getPayload(JWT_KEY_STAFF_ID)));
        authContext.setMerchantId(parseLong(jwt.getPayload(JWT_KEY_MERCHANT_ID)));
        authContext.setStoreId(parseLong(jwt.getPayload(JWT_KEY_STORE_ID)));
        authContext.setRoleCodes(parseList(jwt.getPayload(JWT_KEY_ROLE_CODES)));
        authContext.setPermissionCodes(parseList(jwt.getPayload(JWT_KEY_PERMISSION_CODES)));
        if (StringUtils.isBlank(authContext.getUserType()) && StringUtils.isNotBlank(authContext.getUserId())) {
            authContext.setUserType(WxMiniAuthContext.USER_TYPE_WX_USER);
        }
        return authContext;
    }

    private String parseString(Object value) {
        return value == null ? "" : value.toString();
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<String> parseList(Object value) {
        if (value == null) {
            return new ArrayList<>();
        }
        String raw = value.toString();
        if (raw.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String item : raw.split(",")) {
            if (StringUtils.isBlank(item)) {
                continue;
            }
            result.add(item.trim());
        }
        return result;
    }

    private String joinList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (StringUtils.isBlank(value)) {
                continue;
            }
            result.add(value.trim());
        }
        return String.join(",", result);
    }
}
