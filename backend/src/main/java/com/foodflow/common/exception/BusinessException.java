package com.foodflow.common.exception;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException() {
        this("BUSINESS_ERROR", "业务处理失败");
    }

    public BusinessException(String msg) {
        this("BUSINESS_ERROR", msg);
    }

    public BusinessException(String errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
