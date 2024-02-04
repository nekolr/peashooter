package com.github.nekolr.peashooter.security;

import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.config.UserSettings;
import com.github.nekolr.peashooter.controller.request.auth.LoginUser;
import com.github.nekolr.peashooter.service.IUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final IUserService userService;
    private final UserSettings userSettings;
    private final SettingsManager settingsManager;

    private static final String API_KEY_PARAM = "apiKey";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        this.handleApiKey(request);
        chain.doFilter(request, response);
    }

    private void handleApiKey(HttpServletRequest request) {
        String apiKey = request.getParameter(API_KEY_PARAM);
        if (settingsManager.validApiKey(apiKey)) {
            LoginUser loginUser = userService.findByUsername(userSettings.getUsername());
            if (Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(loginUser, null, null);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
    }
}
