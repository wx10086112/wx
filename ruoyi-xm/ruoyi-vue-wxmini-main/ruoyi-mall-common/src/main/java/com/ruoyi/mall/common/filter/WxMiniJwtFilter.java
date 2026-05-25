package com.ruoyi.mall.common.filter;

import com.ruoyi.mall.common.bo.WxMiniAuthContext;
import com.ruoyi.mall.common.service.IWxMiniJwtService;
import com.ruoyi.mall.common.util.WxMiniUserContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class WxMiniJwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(WxMiniJwtFilter.class);

    @Resource
    private IWxMiniJwtService jwtService;

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

            // 商家端：通过AppID识别商家，token仅用于权限控制
            if (path.startsWith("/wxmini/merchant-mini")) {
                handleMerchantMiniRequest(request, response, filterChain);
                return;
            }

            // C端和其他wxmini接口：需要有效token
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
     * 商家端请求处理：AppID确定商家，token确定权限
     */
    private void handleMerchantMiniRequest(HttpServletRequest request, HttpServletResponse response,
                                           FilterChain filterChain) throws ServletException, IOException {
        // 登录接口直接放行
        if (request.getRequestURI().startsWith("/wxmini/merchant-mini/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 读取AppID（由商家端小程序固定传入）
        String appId = request.getHeader("X-Merchant-AppId");
        if (StringUtils.isNotBlank(appId)) {
            // AppID有效，允许通过（商家ID由Controller从AppID解析并设置）
            // 如果同时携带了有效token，也设置权限上下文
            String token = request.getHeader("Wx-Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    String jwt = token.substring(7);
                    if (jwtService.verifyToken(jwt)) {
                        WxMiniAuthContext authContext = jwtService.parseAuthContext(jwt);
                        WxMiniUserContext.setCurrentUserContext(authContext);
                    }
                } catch (Exception e) {
                    // token无效不影响AppID的使用，只是没有权限信息
                    log.debug("商家端token验证失败，仅使用AppID: {}", e.getMessage());
                }
            }
            filterChain.doFilter(request, response);
            return;
        }

        // 没有AppID，需要token
        String token = request.getHeader("Wx-Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"缺少商家标识\"}");
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
            WxMiniUserContext.setCurrentUserContext(authContext);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean checkIsExcludeUri(String path) {
        return path.startsWith("/wxmini/login") || path.startsWith("/wxmini/portal")
                || path.startsWith("/wxmini/pay/notify")
                || path.startsWith("/wxmini/template/config")
                || path.startsWith("/wxmini/user/phone/bind")
                || path.startsWith("/wxmini/merchant/list") || path.startsWith("/wxmini/merchant/detail")
                || path.startsWith("/wxmini/merchant/home")
                || path.startsWith("/wxmini/groupon/list") || path.startsWith("/wxmini/groupon/detail");
    }
}
