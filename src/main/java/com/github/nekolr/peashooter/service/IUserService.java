package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.controller.request.auth.LoginUser;
import com.github.nekolr.peashooter.controller.response.auth.LoginUserVo;
import com.github.nekolr.peashooter.controller.response.auth.UserInfo;

public interface IUserService {

    LoginUser findByUsername(String username);

    LoginUserVo login(LoginUser loginUser);

    UserInfo userinfo();
}
