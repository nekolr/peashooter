package com.github.nekolr.peashooter.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // 关闭登出
                .logout().disable()
                // 关闭 csrf
                .csrf().disable()

                // X-Frame-Options: SAMEORIGIN
                .headers()
                .frameOptions()
                .sameOrigin()

                // X-Content-Type-Options: nosniff
                .and()
                .headers()
                .contentTypeOptions()

                // X-XSS-Protection: 1; mode=block
                .and().and()
                .headers()
                .xssProtection()
                .xssProtectionEnabled(true)

                // 授权异常处理
                .and().and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)

                // 不需要 session
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // 过滤请求
                .and()
                .authorizeRequests()
                // OPTIONS 预检请求可以匿名访问
                .antMatchers(HttpMethod.OPTIONS, "/**").anonymous()
                // 主页可以匿名访问
                .antMatchers(HttpMethod.GET, "/").anonymous()
                // 登录请求不拦截（如果登录请求头包含 Authorization: Bearer 任意字符，那么还是会进行校验）
                .antMatchers(HttpMethod.POST, "/auth/login").permitAll()

                // 其他所有请求都要经过验证
                .anyRequest().authenticated();

        httpSecurity
                // 添加登录和权限校验的两个过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
