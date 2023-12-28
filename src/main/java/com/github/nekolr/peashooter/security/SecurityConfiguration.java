package com.github.nekolr.peashooter.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity

                // 关闭 csrf
                .csrf(AbstractHttpConfigurer::disable)

                // X-Frame-Options: SAMEORIGIN
                .headers(configurer -> configurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // X-Content-Type-Options: nosniff
                .headers(configurer -> configurer.contentTypeOptions(Customizer.withDefaults()))

                // X-XSS-Protection: 1; mode=block
                .headers(configurer -> configurer.xssProtection(config -> config.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)))

                // 不需要 session
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 过滤请求
                .authorizeHttpRequests(configurer ->
                        // OPTIONS 预检请求可以匿名访问
                        configurer.requestMatchers(HttpMethod.OPTIONS, "/**").anonymous()
                                // 主页可以匿名访问
                                .requestMatchers(HttpMethod.GET, "/").anonymous()
                                .requestMatchers(HttpMethod.GET, "/index.html").anonymous()
                                // 登录请求不拦截
                                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                                // 种子下载不拦截
                                .requestMatchers(HttpMethod.GET, "/api/torrents").permitAll()
                                // 静态资源可以匿名访问
                                .requestMatchers(HttpMethod.GET, "/assets/**").anonymous()
                                .requestMatchers(HttpMethod.GET, "/resource/**").anonymous()
                                .requestMatchers(HttpMethod.GET, "/favicon.ico").anonymous()
                                .requestMatchers(HttpMethod.GET, "/favicon.png").anonymous()
                                // 前端路由可以匿名访问
                                .requestMatchers(HttpMethod.GET, "/login").anonymous()
                                .requestMatchers(HttpMethod.GET, "/home").anonymous()
                                .requestMatchers(HttpMethod.GET, "/404").anonymous()
                                .requestMatchers(HttpMethod.GET, "/system/**").anonymous()
                                .requestMatchers(HttpMethod.GET, "/group/**").anonymous()
                                .requestMatchers(HttpMethod.GET, "/datasource/**").anonymous()
                                // 其他所有请求都要经过验证
                                .anyRequest().authenticated());

        httpSecurity
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
