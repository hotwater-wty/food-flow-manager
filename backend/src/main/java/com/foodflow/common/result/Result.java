package com.foodflow.common.result;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "统一响应结果")
public class Result<T> implements Serializable {
    @Schema(description = "业务状态码：1-成功，0-失败", example = "1")
    private Integer code;

    @Schema(description = "响应消息", example = "success")
    private String msg;

    @Schema(description = "稳定业务错误码，成功时为空", example = "TABLE_IN_USE")
    private String errorCode;

    @Schema(description = "响应数据")
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 1;
        result.msg = "success";
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<T>();
        result.code = 1;
        result.msg = "success";
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        return error("BUSINESS_ERROR", msg);
    }

    public static <T> Result<T> error(String errorCode, String msg) {
        Result<T> result = new Result<T>();
        result.code = 0;
        result.msg = msg;
        result.errorCode = errorCode;
        return result;
    }
}
