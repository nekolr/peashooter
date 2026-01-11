package com.github.nekolr.peashooter.exception;

import com.github.nekolr.peashooter.dto.JsonBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有未知异常
     */
    @ExceptionHandler(value = Exception.class)
    public JsonBean<Void> handleException(Exception e) {
        log.error("出现未知异常", e);
        return JsonBean.fail(e.getMessage());
    }

}
