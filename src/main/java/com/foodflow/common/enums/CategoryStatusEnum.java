package com.foodflow.common.enums;

import lombok.Getter;

@Getter
public enum CategoryStatusEnum {
    ENABLED(1, "启用"),
    DISABLED(2, "禁用");

    private final Integer code;
    private final String description;

    CategoryStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    // 静态工厂方法，用以返回该枚举类中的枚举值
    public static CategoryStatusEnum ofCode(Integer code) {
        for (CategoryStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown category status code: " + code);
    }
}
