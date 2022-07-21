package com.github.nekolr.peashooter.security;

import com.github.nekolr.peashooter.controller.req.auth.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MyContextHolder {

    public LoginUser currentUser() {
        LoginUser user;
        try {
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication auth = context.getAuthentication();
            user = (LoginUser) auth.getPrincipal();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return user;
    }
}
