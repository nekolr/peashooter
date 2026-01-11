package com.github.nekolr.peashooter.dto;

public record JsonBean<T>(String msg, Boolean success, T data) {

    public static JsonBean<Void> ok() {
        return new JsonBean<>("success", Boolean.TRUE, null);
    }

    public static <T> JsonBean<T> ok(T data) {
        return new JsonBean<>("success", Boolean.TRUE, data);
    }

    public static JsonBean<Void> fail() {
        return new JsonBean<>("failed", Boolean.FALSE, null);
    }

    public static JsonBean<Void> fail(String msg) {
        return new JsonBean<>(msg, Boolean.FALSE, null);
    }
}
