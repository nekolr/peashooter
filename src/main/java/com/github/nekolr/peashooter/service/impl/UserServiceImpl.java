package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.config.UserSettings;
import com.github.nekolr.peashooter.controller.cmd.auth.LoginCmd;
import com.github.nekolr.peashooter.controller.vo.auth.LoginVo;
import com.github.nekolr.peashooter.controller.vo.auth.UserInfoVo;
import com.github.nekolr.peashooter.dto.UserInfoDto;
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
    public UserInfoDto findByUsername(String username) {
        if (userSettings.getUsername().equals(username)) {
            return new UserInfoDto(userSettings.getUsername(), userSettings.getPassword());
        }
        return null;
    }

    @Override
    public LoginVo login(LoginCmd cmd) {
        UserInfoDto userInfoDto = findByUsername(cmd.getUsername());
        if (Objects.isNull(userInfoDto) || !cmd.getPassword().equals(userInfoDto.password())) {
            throw new RuntimeException("无效的用户名或密码");
        }
        String token = tokenProvider.createToken(cmd.getUsername());
        UserInfoVo userInfoVo = new UserInfoVo(cmd.getUsername());
        return new LoginVo(token, userInfoVo);
    }

    @Override
    public UserInfoVo currentUserInfo() {
        String username = contextHolder.getCurrUsername();
        if (Objects.isNull(username)) {
            throw new RuntimeException("用户未登录");
        }
        return new UserInfoVo(username);
    }
}
