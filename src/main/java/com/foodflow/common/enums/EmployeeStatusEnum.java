package com.foodflow.common.enums;

import lombok.Getter;

@Getter
public enum EmployeeStatusEnum {
    NORMAL(1, "正常"),
    DISABLED(2, "禁用"),
    RESIGNED(3, "离职");

    private final Integer code;
    private final String description;

    EmployeeStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static EmployeeStatusEnum ofCode(Integer code) {
        for (EmployeeStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown employee status code: " + code);
    }
}
