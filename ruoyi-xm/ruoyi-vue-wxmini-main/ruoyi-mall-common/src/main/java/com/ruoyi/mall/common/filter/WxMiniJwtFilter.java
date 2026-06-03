package com.ruoyi.mall.common.filter;

import com.ruoyi.mall.common.bo.WxMiniAuthContext;
import com.ruoyi.mall.common.service.IWxMiniJwtService;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

@Component
public class WxMiniJwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(WxMiniJwtFilter.class);

    @Resource
    private IWxMiniJwtService jwtService;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            if (this.checkIsExcludeUri(path)) {
                filterChain.doFilter(request, response);
                return;
            }
            if (!path.startsWith("/wxmini")) {
                filterChain.doFilter(request, response);
                return;
            }

            // 商家端：必须校验JWT，X-Merchant-AppId仅作租户识别
            if (path.startsWith("/wxmini/merchant-mini")) {
                handleMerchantMiniRequest(request, response, filterChain);
                return;
            }

            resolveCAppIdMerchantContext(request);
            if (isWxMiniPublicBrowseUri(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            // C端其他wxmini接口：需要有效token
            String token = request.getHeader("Wx-Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"未登录\"}");
                return;
            }

            token = token.substring(7);
            try {
                if (!jwtService.verifyToken(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期\"}");
                    return;
                }
                WxMiniAuthContext authContext = jwtService.parseAuthContext(token);
                if (StringUtils.isEmpty(authContext.getUserId())) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期\"}");
                    return;
                }
                WxMiniUserContext.setCurrentUserContext(authContext);
            } catch (Exception e) {
                log.error("JWT验证失败: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期\"}");
                return;
            }

            filterChain.doFilter(request, response);
        } finally {
            WxMiniUserContext.clear();
        }
    }

    /**
     * 公开浏览接口也需要先按 C 端 AppID 识别商家，否则商家列表、团购列表会失去租户上下文。
     */
    private void resolveCAppIdMerchantContext(HttpServletRequest request) {
        String cAppId = request.getHeader("X-Wx-AppId");
        if (StringUtils.isBlank(cAppId)) {
            return;
        }
        Long cMerchantId = resolveMerchantIdByCAppId(cAppId);
        if (cMerchantId != null) {
            WxMiniUserContext.setAppIdMerchantId(cMerchantId);
        }
    }

    /**
     * 商家端请求处理：入口二维码负责指定商家，JWT负责确认登录员工。
     */
    private void handleMerchantMiniRequest(HttpServletRequest request, HttpServletResponse response,
                                           FilterChain filterChain) throws ServletException, IOException {
        if (isMerchantMiniPublicUri(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 所有其他商家端接口必须要求有效JWT
        String token = request.getHeader("Wx-Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录\"}");
            return;
        }

        token = token.substring(7);
        try {
            if (!jwtService.verifyToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期\"}");
                return;
            }
            WxMiniAuthContext authContext = jwtService.parseAuthContext(token);
            if (StringUtils.isEmpty(authContext.getUserId())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期\"}");
                return;
            }

            String requestMerchantId = request.getHeader("X-Merchant-Id");
            if (StringUtils.isNotBlank(requestMerchantId)
                    && authContext.getMerchantId() != null
                    && !requestMerchantId.equals(String.valueOf(authContext.getMerchantId()))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"商家入口与登录账号不匹配\"}");
                return;
            }

            // BC合并后商家端也运行在C端小程序内，保留旧M端AppID兼容。
            String appId = request.getHeader("X-Merchant-AppId");
            if (StringUtils.isNotBlank(appId) && authContext.getMerchantId() != null) {
                if (!validateMerchantAppId(authContext.getMerchantId(), appId)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"msg\":\"商家身份校验失败\"}");
                    return;
                }
            }

            WxMiniUserContext.setCurrentUserContext(authContext);
        } catch (Exception e) {
            log.error("商家端JWT验证失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 通过ApplicationContext动态获取IMerchantService，校验AppID与merchantId是否匹配。
     * BC端合并后以C端AppID为主，旧M端AppID仅用于兼容历史独立商家端。
     */
    private boolean validateMerchantAppId(Long merchantId, String appId) {
        try {
            Object merchantService = applicationContext.getBean("merchantServiceImpl");
            Method selectById = merchantService.getClass().getMethod("selectMerchantById", Long.class);
            Object merchant = selectById.invoke(merchantService, merchantId);
            if (merchant == null) {
                return false;
            }

            String cMiniAppId = readStringProperty(merchant, "getCMiniAppId");
            String mMiniAppId = readStringProperty(merchant, "getMMiniAppId");
            return appId.equals(cMiniAppId) || appId.equals(mMiniAppId);
        } catch (Exception e) {
            log.error("商家AppID校验异常: merchantId={}, appId={}", merchantId, appId, e);
            return false;
        }
    }

    private String readStringProperty(Object target, String getterName) throws ReflectiveOperationException {
        Method getter = target.getClass().getMethod(getterName);
        Object value = getter.invoke(target);
        return value instanceof String ? (String) value : null;
    }

    /**
     * 通过C端小程序AppID解析商家ID
     */
    private Long resolveMerchantIdByCAppId(String appId) {
        try {
            Object merchantService = applicationContext.getBean("merchantServiceImpl");
            Method selectByCAppId = merchantService.getClass().getMethod("selectMerchantByCAppId", String.class);
            Object merchant = selectByCAppId.invoke(merchantService, appId);
            if (merchant == null) {
                return null;
            }
            Method getId = merchant.getClass().getMethod("getId");
            return (Long) getId.invoke(merchant);
        } catch (Exception e) {
            log.error("C端AppID解析商家ID异常: appId={}", appId, e);
            return null;
        }
    }

    /**
     * 白名单：仅放行登录、门户、公开数据、支付回调等无需登录的接口
     * 注意：/wxmini/login/test 不在白名单中，需要配置开启 + token 才能访问
     */
    private boolean checkIsExcludeUri(String path) {
        return path.equals("/wxmini/login")
                || path.startsWith("/wxmini/portal")
                || path.startsWith("/wxmini/public")
                || path.startsWith("/wxmini/pay/notify")
                || path.startsWith("/wxmini/template/config")
                || isMerchantMiniPublicUri(path);
    }

    private boolean isWxMiniPublicBrowseUri(String path) {
        return path.equals("/wxmini/merchant/home")
                || path.equals("/wxmini/merchant/list")
                || path.startsWith("/wxmini/merchant/detail/")
                || path.startsWith("/wxmini/merchant/album/")
                || path.equals("/wxmini/groupon/list")
                || path.startsWith("/wxmini/groupon/detail/");
    }

    private boolean isMerchantMiniPublicUri(String path) {
        return path.startsWith("/wxmini/merchant-mini/auth/login")
                || path.startsWith("/wxmini/merchant-mini/entry/");
    }
}
