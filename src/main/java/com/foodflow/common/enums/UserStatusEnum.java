package com.foodflow.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum UserStatusEnum implements IEnum<Integer> {
    NORMAL(1, "正常"),
    DISABLED(2, "禁用");

    @EnumValue
    @JsonValue
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

    @Override
    public Integer getValue() {
        return code;
    }
}
