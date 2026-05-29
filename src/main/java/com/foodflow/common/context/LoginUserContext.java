package com.foodflow.common.context;

public final class LoginUserContext {

    // 使用 ThreadLocal 保存当前请求线程中的登录用户 ID，避免业务方法层层传参。
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    private LoginUserContext() {
    }

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    // 请求结束后必须清理，防止线程复用时读到上一次请求的用户 ID。
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
