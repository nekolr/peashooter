package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.controller.req.auth.LoginUser;
import com.github.nekolr.peashooter.controller.rsp.auth.LoginUserVo;
import com.github.nekolr.peashooter.controller.rsp.auth.UserInfo;

public interface IUserService {

    LoginUser findByUsername(String username);

    LoginUserVo login(LoginUser loginUser);

    UserInfo userinfo();
}
