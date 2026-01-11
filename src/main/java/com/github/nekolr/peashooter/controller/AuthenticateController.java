package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.controller.vo.auth.UserInfoVo;
import com.github.nekolr.peashooter.dto.JsonBean;
import com.github.nekolr.peashooter.controller.cmd.auth.LoginCmd;
import com.github.nekolr.peashooter.controller.vo.auth.LoginVo;
import com.github.nekolr.peashooter.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticateController {

    private final IUserService userService;

    @PostMapping("/login")
    public JsonBean<LoginVo> login(@RequestBody LoginCmd cmd) {
        return JsonBean.ok(userService.login(cmd));
    }

    @GetMapping("userinfo")
    public JsonBean<UserInfoVo> currentUserInfo() {
        return JsonBean.ok(userService.currentUserInfo());
    }

}
