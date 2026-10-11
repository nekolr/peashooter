package com.github.nekolr.peashooter.exception;

import com.github.nekolr.peashooter.dto.JsonBean;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.nekolr.peashooter.constant.Peashooter.NEWZNAB_API_PATH;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 判断是否为 Newznab/Torznab 接口请求（这类客户端要求 XML 错误响应）
     */
    private static boolean isNewznabRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith(NEWZNAB_API_PATH);
    }

    /**
     * 向 Newznab/Torznab 客户端返回 XML 错误
     */
    private static void sendNewznabXmlError(HttpServletResponse response, String description) throws IOException {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType(MediaType.APPLICATION_XML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("<error code=\"900\" description=\"" + description + "\"/>");
    }

    /**
     * 处理业务校验类异常（参数错误等），返回 400 语义
     */
    @ExceptionHandler(value = {IllegalArgumentException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public JsonBean<Void> handleBadRequest(Exception e) {
        String msg = e instanceof MethodArgumentNotValidException
                ? "请求参数校验失败"
                : e.getMessage();
        log.warn("请求参数错误: {}", msg);
        return JsonBean.fail(msg);
    }

    /**
     * 处理所有未知异常，返回 500 语义，不向前端透出内部异常信息
     */
    @ExceptionHandler(value = Exception.class)
    public JsonBean<Void> handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        log.error("出现未知异常", e);
        if (isNewznabRequest(request)) {
            try {
                sendNewznabXmlError(response, "Internal server error");
            } catch (IOException ignored) {
            }
            return null;
        }
        return JsonBean.fail("服务器内部错误");
    }

}
