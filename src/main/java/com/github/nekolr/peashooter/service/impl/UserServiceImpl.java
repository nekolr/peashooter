package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.config.UserSettings;
import com.github.nekolr.peashooter.controller.req.auth.LoginUser;
import com.github.nekolr.peashooter.controller.rsp.auth.LoginUserVo;
import com.github.nekolr.peashooter.controller.rsp.auth.UserInfo;
import com.github.nekolr.peashooter.security.MyContextHolder;
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
    private final MyContextHolder contextHolder;

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
        String username = contextHolder.currentUser().getUsername();
        return new UserInfo(username);
    }
}
