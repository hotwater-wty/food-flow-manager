package com.foodflow.common.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.foodflow.common.result.Result;

import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException ex) {
        log.error("业务异常", ex);
        return Result.error(ex.getErrorCode(), ex.getMessage());
    }

    /**
     * 方法参数校验异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("方法参数校验异常", ex);
        FieldError fieldError = ex.getBindingResult().getFieldError();

        String message = fieldError != null
                ? fieldError.getDefaultMessage()
                : "参数校验失败";

        return Result.error("VALIDATION_ERROR", message);
    }

    /**
     * 系统全局异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception ex) {
        log.error("系统全局异常", ex);
        return Result.error("SYSTEM_ERROR", "系统处理异常，请稍后重试");
    }

    /**
     * 重复创建异常处理
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException ex) {
        log.error("重复创建异常", ex);
        return Result.error("DUPLICATE_RESOURCE", "重复创建，请稍后重试");
    }

    /** Keep protocol-level failures such as an expired SSE ticket as HTTP errors. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<Void>> handleResponseStatusException(ResponseStatusException ex) {
        String message = ex.getReason() == null ? "请求失败" : ex.getReason();
        return ResponseEntity.status(ex.getStatusCode())
                .body(Result.error("HTTP_" + ex.getStatusCode().value(), message));
    }
}
