package com.github.nekolr.peashooter.security;

import com.github.nekolr.peashooter.controller.req.LoginUser;
import com.github.nekolr.peashooter.service.IUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_HEADER_KEY = "Authorization";
    private static final String TOKEN_HEADER_VALUE_PREFIX = "Bearer ";

    private final IUserService userService;
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        String jwt = this.resolveToken(request);

        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            String username = tokenProvider.getUsername(jwt);
            // 只有在 Authentication 为空时才会放入
            if (Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
                LoginUser loginUser = userService.findByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(loginUser, null, null);

                log.debug("set Authentication to security context for '{}', uri: {}", username, requestURI);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } else {
            log.debug("no valid JWT token found, uri: {}", requestURI);
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER_KEY);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_HEADER_VALUE_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
