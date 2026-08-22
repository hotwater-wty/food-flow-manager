package com.foodflow.common.context;

public final class LoginContext {

    private static final ThreadLocal<LoginInfo> LOGIN_USER_HOLDER = new ThreadLocal<>();

    private LoginContext() {
    }

    public static void set(LoginInfo loginUserInfo) {
        LOGIN_USER_HOLDER.set(loginUserInfo);
    }

    public static LoginInfo get() {
        return LOGIN_USER_HOLDER.get();
    }

    public static Long getUserId() {
        LoginInfo loginUserInfo = LOGIN_USER_HOLDER.get();
        return loginUserInfo == null ? null : loginUserInfo.getUserId();
    }

    public static Long getEmployeeId() {
        LoginInfo loginUserInfo = LOGIN_USER_HOLDER.get();
        return loginUserInfo == null ? null : loginUserInfo.getEmployeeId();
    }

    public static com.foodflow.common.enums.LoginTypeEnum getLoginType() {
        LoginInfo loginUserInfo = LOGIN_USER_HOLDER.get();
        return loginUserInfo == null ? null : loginUserInfo.getLoginType();
    }

    public static com.foodflow.common.enums.EmployeeRoleEnum getEmployeeRole() {
        LoginInfo loginUserInfo = LOGIN_USER_HOLDER.get();
        return loginUserInfo == null ? null : loginUserInfo.getEmployeeRole();
    }

    public static void clear() {
        LOGIN_USER_HOLDER.remove();
    }
}
