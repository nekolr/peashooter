package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.config.UserSettings;
import com.github.nekolr.peashooter.controller.request.auth.LoginUser;
import com.github.nekolr.peashooter.controller.response.auth.LoginUserVo;
import com.github.nekolr.peashooter.controller.response.auth.UserInfo;
import com.github.nekolr.peashooter.security.TokenProvider;
import com.github.nekolr.peashooter.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserSettings userSettings;
    private final TokenProvider tokenProvider;

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
        SecurityContext context = SecurityContextHolder.getContextHolderStrategy().getContext();
        LoginUser loginUser = (LoginUser) context.getAuthentication().getPrincipal();
        String username = loginUser.getUsername();
        return new UserInfo(username);
    }
}
