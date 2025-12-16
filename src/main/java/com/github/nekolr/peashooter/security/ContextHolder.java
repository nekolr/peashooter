package com.github.nekolr.peashooter.security;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ContextHolder {

    private final static ScopedValue<String> USERNAME = ScopedValue.newInstance();

    private String getUsername() {
        return USERNAME.isBound() ? USERNAME.get() : null;
    }

    /**
     * 在指定作用域内执行操作，期间 USER 绑定到指定用户
     *
     * @param username 用户名
     * @param runnable 要执行的操作
     */
    public void runWithUsername(String username, Runnable runnable) {
        ScopedValue.where(USERNAME, username).run(runnable);
    }

    /**
     * 在指定作用域内执行操作并返回结果，期间 USER 绑定到指定用户
     *
     * @param username 用户名
     * @param op       要执行的操作
     * @return 操作结果
     */
    public <R, X extends Throwable> R callWithUsername(String username, ScopedValue.CallableOp<? extends R, X> op) throws X {
        return ScopedValue.where(USERNAME, username).call(op);
    }

    /**
     * 获取用户名
     */
    public String getCurrUsername() {
        return getUsername();
    }
}
