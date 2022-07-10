package com.github.nekolr.peashooter.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class UserSettings {
    /**
     * 用户名
     */
    @Value("${peashooter.username}")
    private String username;

    /**
     * 密码
     */
    @Value("${peashooter.password}")
    private String password;
}
