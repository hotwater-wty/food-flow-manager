package com.foodflow.common.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    NORMAL(1, "正常"),
    DISABLED(2, "禁用");

    private final Integer code;
    private final String description;

    UserStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UserStatusEnum ofCode(Integer code) {
        for (UserStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown user status code: " + code);
    }
}
