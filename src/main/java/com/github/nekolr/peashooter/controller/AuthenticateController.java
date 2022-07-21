package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.controller.rsp.auth.UserInfo;
import com.github.nekolr.peashooter.entity.JsonBean;
import com.github.nekolr.peashooter.controller.req.auth.LoginUser;
import com.github.nekolr.peashooter.controller.rsp.auth.LoginUserVo;
import com.github.nekolr.peashooter.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticateController {

    private final IUserService userService;

    @PostMapping("/login")
    public JsonBean<LoginUserVo> login(@RequestBody LoginUser loginUser) {
        return JsonBean.ok(userService.login(loginUser));
    }

    @GetMapping("userinfo")
    public JsonBean<UserInfo> userinfo() {
        return JsonBean.ok(userService.userinfo());
    }

}
