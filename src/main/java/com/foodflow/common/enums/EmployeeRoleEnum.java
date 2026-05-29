package com.foodflow.common.enums;

import lombok.Getter;

@Getter
public enum EmployeeRoleEnum {
    STAFF(1, "店员"),
    MANAGER(2, "店长");

    private final Integer code;
    private final String description;

    EmployeeRoleEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static EmployeeRoleEnum ofCode(Integer code) {
        for (EmployeeRoleEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown employee role code: " + code);
    }
}
