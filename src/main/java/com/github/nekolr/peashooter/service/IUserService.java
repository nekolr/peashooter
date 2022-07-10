package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.controller.req.LoginUser;
import com.github.nekolr.peashooter.controller.rsp.LoginUserVo;

public interface IUserService {

    LoginUser findByUsername(String username);

    LoginUserVo login(LoginUser loginUser);
}
