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
import org.springframework.http.MediaType;
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

import static com.github.nekolr.peashooter.constant.Peashooter.API_KEY_PARAM;
import static com.github.nekolr.peashooter.constant.Peashooter.CHARSET;
import static com.github.nekolr.peashooter.constant.Peashooter.NEWZNAB_API_PATH;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_HEADER_VALUE_PREFIX = "Bearer ";

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
            if (Objects.isNull(userInfoDto)) {
                sendUnauthorizedResponse(request, response);
                return;
            }
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
                sendUnauthorizedResponse(request, response);
            }
        } else {
            log.debug("no valid JWT token found, uri: {}", requestURI);
            sendUnauthorizedResponse(request, response);
        }
    }

    private void sendUnauthorizedResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Newznab/Torznab 客户端（Sonarr）只解析 XML error，返回 JSON 会表现为难定位的解析/连接失败
        if (isNewznabRequest(request.getRequestURI())) {
            sendNewznabErrorResponse(response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String json = JacksonUtils.tryParse(() ->
                JacksonUtils.getObjectMapper().writeValueAsString(JsonBean.fail("unauthorized")));
        response.getWriter().write(json);
    }

    private boolean isNewznabRequest(String requestURI) {
        return StringUtils.hasText(requestURI) && requestURI.startsWith(NEWZNAB_API_PATH);
    }

    private void sendNewznabErrorResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_XML_VALUE);
        response.setCharacterEncoding(CHARSET);
        response.getWriter().write("<error code=\"100\" description=\"Missing or invalid apikey\"/>");
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_HEADER_VALUE_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
