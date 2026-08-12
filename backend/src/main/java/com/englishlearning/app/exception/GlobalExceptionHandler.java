package com.englishlearning.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

/**
 * 全局异常处理。
 *
 * 关键点：MaxUploadSizeExceededException 这类异常发生在 Spring 解析请求体（multipart）阶段，
 * 早于 Controller 方法真正被调用，所以在 Controller 里写 try/catch 是catch不到的，
 * 必须用 @RestControllerAdvice 在更外层统一拦截，直接返回 JSON，
 * 避免异常被转发到 /error 后又被 Spring Security 拦一次导致连接异常。
 *
 * 处理器按"从具体到通用"排列，Spring 会自动匹配最贴合异常类型的那个：
 *   1) MaxUploadSizeExceededException —— 文件太大
 *   2) RuntimeException —— 业务逻辑里主动抛出的、已经写好中文提示的异常
 *   3) Exception —— 兜底，任何没被前两条捕获的意外错误，
 *      至少让前端能看到一条真实的错误信息，而不是一个不明所以的 500。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("message", "文件太大，超出了上传大小限制，请压缩后重试或联系管理员调整限制"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage() == null ? "请求处理失败" : ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        String detail = ex.getClass().getSimpleName() + (ex.getMessage() != null ? ": " + ex.getMessage() : "");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "服务器处理出错（" + detail + "），请把这条信息反馈给开发者"));
    }
}