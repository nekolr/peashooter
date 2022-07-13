package com.github.nekolr.peashooter.controller.rsp;

import com.github.nekolr.peashooter.controller.req.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class LoginUserVo implements Serializable {

    private String token;

    private LoginUser user;
}
