package com.github.nekolr.peashooter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableCaching
@EnableRetry
@EnableScheduling
@EnableWebSecurity
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class, scanBasePackages = "com.github.nekolr")
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class PeashooterApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeashooterApplication.class, args);
    }

}
