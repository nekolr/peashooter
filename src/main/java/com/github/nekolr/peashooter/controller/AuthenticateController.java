package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.entity.JsonBean;
import com.github.nekolr.peashooter.controller.req.LoginUser;
import com.github.nekolr.peashooter.controller.rsp.LoginUserVo;
import com.github.nekolr.peashooter.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthenticateController {

    private final IUserService userService;

    @PostMapping("/login")
    public JsonBean<LoginUserVo> login(@RequestBody LoginUser loginUser) {
        return JsonBean.ok(userService.login(loginUser));
    }

}
