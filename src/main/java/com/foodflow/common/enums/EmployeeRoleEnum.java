package com.foodflow.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum EmployeeRoleEnum implements IEnum<Integer> {
    STAFF(1, "店员"),
    MANAGER(2, "店长");

    @EnumValue
    @JsonValue
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

    @Override
    public Integer getValue() {
        return code;
    }
}
