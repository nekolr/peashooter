package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.config.UserSettings;
import com.github.nekolr.peashooter.controller.request.auth.LoginUser;
import com.github.nekolr.peashooter.controller.response.auth.LoginUserVo;
import com.github.nekolr.peashooter.controller.response.auth.UserInfo;
import com.github.nekolr.peashooter.security.ContextHolder;
import com.github.nekolr.peashooter.security.TokenProvider;
import com.github.nekolr.peashooter.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserSettings userSettings;
    private final TokenProvider tokenProvider;
    private final ContextHolder contextHolder;

    @Override
    public LoginUser findByUsername(String username) {
        if (userSettings.getUsername().equals(username)) {
            return new LoginUser(userSettings.getUsername(), userSettings.getPassword());
        }
        return null;
    }

    @Override
    public LoginUserVo login(LoginUser loginUser) {
        LoginUser entity = findByUsername(loginUser.getUsername());
        if (Objects.isNull(entity) || !loginUser.getPassword().equals(entity.getPassword())) {
            throw new RuntimeException("无效的用户名或密码");
        }
        String token = tokenProvider.createToken(loginUser.getUsername());
        return new LoginUserVo(token, loginUser);
    }

    @Override
    public UserInfo userinfo() {
        String username = contextHolder.getCurrUsername();
        if (Objects.isNull(username)) {
            throw new RuntimeException("用户未登录");
        }
        return new UserInfo(username);
    }
}
