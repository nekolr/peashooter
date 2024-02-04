package com.github.nekolr.peashooter.controller.response.auth;

import com.github.nekolr.peashooter.controller.request.auth.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class LoginUserVo implements Serializable {

    private String token;

    private LoginUser user;
}
