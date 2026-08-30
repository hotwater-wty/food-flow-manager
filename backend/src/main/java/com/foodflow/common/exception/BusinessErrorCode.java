package com.foodflow.common.exception;

/**
 * 稳定的业务错误码注册表。
 *
 * <p>错误码供前端分支、自动化测试和日志统计使用；defaultMessage 只是默认的人类可读提示，
 * 不应被前端当作机器协议。</p>
 */
public enum BusinessErrorCode {

    TABLE_NOT_FOUND("TABLE_NOT_FOUND", "桌位不存在", "TABLE", false, 404),
    TABLE_DISABLED("TABLE_DISABLED", "桌位已被禁用", "TABLE", false, 409),
    TABLE_IN_USE("TABLE_IN_USE", "桌位已被占用", "TABLE", false, 409),
    TABLE_BUSY_CANNOT_UPDATE("TABLE_BUSY_CANNOT_UPDATE", "业务状态的桌位不能更新", "TABLE", false, 409),
    TABLE_BUSY_CANNOT_DELETE("TABLE_BUSY_CANNOT_DELETE", "业务状态的桌位不能删除", "TABLE", false, 409),
    TABLE_BUSY_CANNOT_DISABLE("TABLE_BUSY_CANNOT_DISABLE", "当前桌位处于业务状态，不能禁用", "TABLE", false, 409),
    TABLE_ALREADY_ENABLED("TABLE_ALREADY_ENABLED", "不能重复启用", "TABLE", false, 409),
    TABLE_ALREADY_DISABLED("TABLE_ALREADY_DISABLED", "不能重复禁用", "TABLE", false, 409),
    TABLE_STATE_UPDATE_FAILED("TABLE_STATE_UPDATE_FAILED", "桌位状态更新失败，请重试", "TABLE", true, 409),

    SESSION_NOT_FOUND("SESSION_NOT_FOUND", "会话不存在", "SESSION", false, 404),
    SESSION_CONFLICT("SESSION_CONFLICT", "当前用户已存在用餐会话", "SESSION", false, 409),
    SESSION_FORBIDDEN("SESSION_FORBIDDEN", "会话不属于当前用户", "SESSION", false, 403),
    INVALID_SESSION_STATE("INVALID_SESSION_STATE", "会话状态错误", "SESSION", false, 409),
    SESSION_TABLE_NOT_FOUND("SESSION_TABLE_NOT_FOUND", "用餐会话关联的餐桌不存在", "SESSION", false, 409),
    SESSION_TABLE_MISMATCH("SESSION_TABLE_MISMATCH", "桌位与会话匹配异常", "SESSION", false, 409),
    SESSION_HAS_UNFINISHED_ORDERS("SESSION_HAS_UNFINISHED_ORDERS", "有订单未完成，不能关闭会话", "SESSION", false, 409),
    SESSION_UPDATE_FAILED("SESSION_UPDATE_FAILED", "用餐会话状态更新失败", "SESSION", true, 409),

    RESERVATION_NOT_FOUND("RESERVATION_NOT_FOUND", "预约不存在", "RESERVATION", false, 404),
    INVALID_RESERVATION_STATE("INVALID_RESERVATION_STATE", "预约状态错误，请重试", "RESERVATION", false, 409),
    RESERVATION_FORBIDDEN("RESERVATION_FORBIDDEN", "只能操作自己的预约", "RESERVATION", false, 403),
    RESERVATION_TABLE_OCCUPIED("RESERVATION_TABLE_OCCUPIED", "桌位已被占用", "RESERVATION", false, 409),
    RESERVATION_CAPACITY_EXCEEDED("RESERVATION_CAPACITY_EXCEEDED", "预约人数超过桌位容量", "RESERVATION", false, 400),
    RESERVATION_TIME_IN_PAST("RESERVATION_TIME_IN_PAST", "预约时间不能早于当前时间", "RESERVATION", false, 400),
    RESERVATION_TOO_FAR("RESERVATION_TOO_FAR", "预约天数不能超过3天", "RESERVATION", false, 400),
    RESERVATION_STATE_UPDATE_FAILED("RESERVATION_STATE_UPDATE_FAILED", "预约状态更新失败，请重试", "RESERVATION", true, 409),

    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "订单不存在", "ORDER", false, 404),
    ORDER_FORBIDDEN("ORDER_FORBIDDEN", "只能查看自己的订单", "ORDER", false, 403),
    INVALID_ORDER_STATE("INVALID_ORDER_STATE", "订单状态流转不合法", "ORDER", false, 409),
    ORDER_COMPLETED_CANNOT_UPDATE("ORDER_COMPLETED_CANNOT_UPDATE", "订单已完成或已取消，不能更新状态", "ORDER", false, 409),
    ORDER_STATUS_REQUIRED("ORDER_STATUS_REQUIRED", "订单状态不能为空", "ORDER", false, 400),
    ORDER_CANCEL_UNSUPPORTED("ORDER_CANCEL_UNSUPPORTED", "暂不支持取消订单", "ORDER", false, 409),
    ORDER_SESSION_NOT_FOUND("ORDER_SESSION_NOT_FOUND", "会话不存在", "ORDER", false, 404),
    ORDER_SESSION_INVALID_STATE("ORDER_SESSION_INVALID_STATE", "会话状态错误", "ORDER", false, 409),
    ORDER_SESSION_TABLE_MISMATCH("ORDER_SESSION_TABLE_MISMATCH", "桌位与会话匹配异常", "ORDER", false, 409),
    DISH_UNAVAILABLE("DISH_UNAVAILABLE", "菜品不存在或已下架", "ORDER", false, 409),

    DISH_NOT_FOUND("DISH_NOT_FOUND", "菜品不存在", "DISH", false, 404),
    DISH_ID_REQUIRED("DISH_ID_REQUIRED", "菜品ID不能为空", "DISH", false, 400),
    DISH_OFF_SALE("DISH_OFF_SALE", "菜品已停售", "DISH", false, 409),
    DISH_SOLD_OUT("DISH_SOLD_OUT", "菜品已售罄", "DISH", false, 409),
    DISH_CATEGORY_NOT_FOUND("DISH_CATEGORY_NOT_FOUND", "菜品分类不存在", "DISH", false, 404),
    DISH_CATEGORY_DISABLED("DISH_CATEGORY_DISABLED", "菜品分类已禁用", "DISH", false, 409),
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "分类不存在", "DISH", false, 404),
    CATEGORY_ALREADY_ENABLED("CATEGORY_ALREADY_ENABLED", "分类已启用", "DISH", false, 409),
    CATEGORY_ENABLE_FAILED("CATEGORY_ENABLE_FAILED", "启用分类失败", "DISH", true, 409),
    CATEGORY_ALREADY_DISABLED("CATEGORY_ALREADY_DISABLED", "分类已禁用", "DISH", false, 409),
    CATEGORY_DISABLE_FAILED("CATEGORY_DISABLE_FAILED", "禁用分类失败", "DISH", true, 409),

    USER_INVALID_CREDENTIALS("USER_INVALID_CREDENTIALS", "手机号或密码错误", "AUTH", false, 401),
    USER_DISABLED("USER_DISABLED", "用户账号不可用", "AUTH", false, 403),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "用户已存在", "AUTH", false, 409),
    EMPLOYEE_PHONE_EXISTS("EMPLOYEE_PHONE_EXISTS", "该手机号已注册员工账号", "AUTH", false, 409),
    EMPLOYEE_INVALID_CREDENTIALS("EMPLOYEE_INVALID_CREDENTIALS", "手机号或密码错误", "AUTH", false, 401),
    EMPLOYEE_DISABLED("EMPLOYEE_DISABLED", "员工账号不可用", "AUTH", false, 403),
    OWNER_CANNOT_DISABLE("OWNER_CANNOT_DISABLE", "店长账号不可禁用", "AUTH", false, 403),
    SELF_DISABLE_FORBIDDEN("SELF_DISABLE_FORBIDDEN", "不能禁用当前登录的账号", "AUTH", false, 403),
    EMPLOYEE_ALREADY_DISABLED("EMPLOYEE_ALREADY_DISABLED", "员工账号已禁用", "AUTH", false, 409),
    EMPLOYEE_DISABLE_FAILED("EMPLOYEE_DISABLE_FAILED", "员工账号禁用失败", "AUTH", true, 409),
    EMPLOYEE_ALREADY_ENABLED("EMPLOYEE_ALREADY_ENABLED", "员工账号已启用", "AUTH", false, 409),
    EMPLOYEE_ENABLE_FAILED("EMPLOYEE_ENABLE_FAILED", "员工账号启用失败", "AUTH", true, 409),
    EMPLOYEE_NOT_FOUND("EMPLOYEE_NOT_FOUND", "员工不存在", "AUTH", false, 404),

    SUBMIT_TOKEN_INVALID("SUBMIT_TOKEN_INVALID", "提交令牌无效或已过期", "PROTOCOL", false, 409),
    SUBMIT_TOKEN_REPLAYED("SUBMIT_TOKEN_REPLAYED", "请勿重复提交", "PROTOCOL", false, 409),
    SUBMIT_SCENE_INVALID("SUBMIT_SCENE_INVALID", "提交场景不合法", "PROTOCOL", false, 400);

    private final String code;
    private final String defaultMessage;
    private final String domain;
    private final boolean retryable;
    private final int httpStatus;

    BusinessErrorCode(String code, String defaultMessage, String domain, boolean retryable, int httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.domain = domain;
        this.retryable = retryable;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String getDomain() {
        return domain;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
