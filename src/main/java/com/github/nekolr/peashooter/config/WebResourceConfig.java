package com.github.nekolr.peashooter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebResourceConfig implements WebMvcConfigurer {

    @Value("${cors.allowOrigin}")
    private String allowOrigin;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源映射
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowOrigin)
                .allowCredentials(true)
                .allowedMethods("*")
                .allowedHeaders("*");
    }

    /**
     * 与一般的前后端分离项目不同，vue 打包的静态资源放在了 resources 目录下，也就是纳入了 spring mvc 的管理范围。
     * 在刷新页面时，请求会经过 spring mvc 处理，而前端路由没有在 mvc 中注册，所以找不到。此时通过将请求 forward
     * 到根目录的方式来将路由交给前端处理，此时浏览器地址没有发生变化（如果使用重定向会真的跳到首页）。
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("forward:/");
        registry.addViewController("/home").setViewName("forward:/");
        registry.addViewController("/404").setViewName("forward:/");
        registry.addViewController("/system/**").setViewName("forward:/");
        registry.addViewController("/group/**").setViewName("forward:/");
        registry.addViewController("/datasource/**").setViewName("forward:/");
    }
}
