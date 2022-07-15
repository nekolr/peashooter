package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.controller.req.LoginUser;
import com.github.nekolr.peashooter.controller.rsp.LoginUserVo;
import com.github.nekolr.peashooter.controller.rsp.UserInfo;

public interface IUserService {

    LoginUser findByUsername(String username);

    LoginUserVo login(LoginUser loginUser);

    UserInfo userinfo();
}
