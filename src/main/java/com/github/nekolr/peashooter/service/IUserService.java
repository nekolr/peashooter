package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.controller.cmd.auth.LoginCmd;
import com.github.nekolr.peashooter.controller.vo.auth.LoginVo;
import com.github.nekolr.peashooter.controller.vo.auth.UserInfoVo;
import com.github.nekolr.peashooter.dto.UserInfoDto;

public interface IUserService {

    /**
     * 根据用户名查询用户信息
     */
    UserInfoDto findByUsername(String username);

    /**
     * 登录
     */
    LoginVo login(LoginCmd cmd);

    /**
     * 获取当前用户信息
     */
    UserInfoVo currentUserInfo();
}
