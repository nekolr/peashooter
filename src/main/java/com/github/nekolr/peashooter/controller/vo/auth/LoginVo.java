package com.github.nekolr.peashooter.controller.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class LoginVo implements Serializable {

    private String token;

    private UserInfoVo user;
}
