package com.github.nekolr.peashooter.entity.dto;

public record JsonBean<T>(String msg, Boolean success, T data) {

    public static JsonBean ok() {
        return new JsonBean("success", Boolean.TRUE, null);
    }

    public static <T> JsonBean ok(T data) {
        return new JsonBean("success", Boolean.TRUE, data);
    }

    public static JsonBean fail() {
        return new JsonBean("failed", Boolean.FALSE, null);
    }

    public static JsonBean fail(String msg) {
        return new JsonBean(msg, Boolean.FALSE, null);
    }
}
