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

/**
 * 微信小程序后台api接口校验过滤器
 *
 * @author weijiayu
 * @date 2025/4/22 23:48
 */
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
            if (!this.checkIsExcludeUri(path) && path.startsWith("/wxmini")) {
                /**
                 * 避免和若依框架默认的认证请求头key名Authorization冲突
                 * @see com.ruoyi.framework.config.SecurityConfig#filterChain
                 * @see com.ruoyi.framework.security.filter.JwtAuthenticationTokenFilter#doFilterInternal
                 */
                String token = request.getHeader("Wx-Authorization");
                if (token == null || !token.startsWith("Bearer ")) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Missing or invalid token");
                    return;
                }

                // 提取JWT Token，去掉 "Bearer "前缀
                token = token.substring(7);
                try {
                    if (!jwtService.verifyToken(token)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Invalid token");
                        return;
                    }
                    WxMiniAuthContext authContext = jwtService.parseAuthContext(token);
                    String userId = authContext.getUserId();
                    if (StringUtils.isEmpty(userId)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("User ID not found in token");
                        return;
                    }
                    WxMiniUserContext.setCurrentUserContext(authContext);
                } catch (Exception e) {
                    String message = e.getMessage();
                    log.error(message, e);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("JWT validation failed: " + message);
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            WxMiniUserContext.clear();
        }
    }

    // 跳过无需鉴权的
    private boolean checkIsExcludeUri(String path) {
        return path.startsWith("/wxmini/login") || path.startsWith("/wxmini/portal")
                || path.startsWith("/wxmini/pay/notify")
                || path.startsWith("/wxmini/merchant-mini/auth/login");
    }
}
