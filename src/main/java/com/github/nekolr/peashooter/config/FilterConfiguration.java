package com.github.nekolr.peashooter.config;

import com.github.nekolr.peashooter.security.AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfiguration {

    private final AuthenticationFilter authenticationFilter;

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilterRegistrationBean() {
        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(authenticationFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1); // 设置过滤器顺序
        return registrationBean;
    }
}