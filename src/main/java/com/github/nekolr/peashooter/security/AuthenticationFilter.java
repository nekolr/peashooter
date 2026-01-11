package com.github.nekolr.peashooter.security;

import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.config.UserSettings;
import com.github.nekolr.peashooter.dto.JsonBean;
import com.github.nekolr.peashooter.dto.UserInfoDto;
import com.github.nekolr.peashooter.service.IUserService;
import com.github.nekolr.peashooter.util.JacksonUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_HEADER_VALUE_PREFIX = "Bearer ";
    private static final String API_KEY_PARAM = "apiKey";

    private final IUserService userService;
    private final UserSettings userSettings;
    private final TokenProvider tokenProvider;
    private final ContextHolder contextHolder;
    private final SettingsManager settingsManager;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 不需要认证的路径列表
     * 使用 Ant 风格路径匹配
     */
    private static final List<String> WHITELIST_PATTERNS = Arrays.asList(
            // 主页
            "/",
            "/index.html",

            // 登录请求
            "/api/auth/login",

            // 静态资源
            "/assets/**",
            "/resource/**",
            "/favicon.ico",
            "/favicon.png",

            // 前端路由
            "/login",
            "/home",
            "/404",
            "/system/**",
            "/group/**",
            "/datasource/**"
    );

    /**
     * 检查请求是否在白名单中
     */
    public boolean isWhitelisted(HttpMethod method, String uri) {
        // OPTIONS 请求全部放行
        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }

        // 遍历白名单模式
        for (String pattern : WHITELIST_PATTERNS) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        if (isWhitelisted(method, requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        String apiKey = request.getParameter(API_KEY_PARAM);
        if (settingsManager.validApiKey(apiKey)) {
            UserInfoDto userInfoDto = userService.findByUsername(userSettings.getUsername());
            // 使用 ScopedValue 在整个过滤器链执行期间绑定用户信息
            contextHolder.runWithUsername(userInfoDto.username(), () -> {
                try {
                    chain.doFilter(request, response);
                } catch (IOException | ServletException e) {
                    log.error("Error processing filter chain", e);
                    throw new RuntimeException(e);
                }
            });
            return;
        }

        String jwt = this.resolveToken(request);

        Claims claims;
        if (StringUtils.hasText(jwt) && (claims = tokenProvider.getClaims(jwt)) != null) {
            String username = claims.getSubject();
            // 只有在没有当前用户时才会放入
            if (Objects.isNull(contextHolder.getCurrUsername())) {
                UserInfoDto userInfoDto = userService.findByUsername(username);
                if (Objects.nonNull(userInfoDto)) {
                    // 使用 ScopedValue 在整个过滤器链执行期间绑定用户信息
                    contextHolder.runWithUsername(userInfoDto.username(), () -> {
                        try {
                            chain.doFilter(request, response);
                        } catch (IOException | ServletException e) {
                            log.error("Error processing filter chain", e);
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    sendUnauthorizedResponse(response);
                }
            } else {
                chain.doFilter(request, response);
            }
        } else {
            log.debug("no valid JWT token found, uri: {}", requestURI);
            sendUnauthorizedResponse(response);
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String json = JacksonUtils.tryParse(() ->
                JacksonUtils.getObjectMapper().writeValueAsString(JsonBean.fail("unauthorized")));
        response.getWriter().write(json);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_HEADER_VALUE_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
