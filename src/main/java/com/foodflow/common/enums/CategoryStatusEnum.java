package com.foodflow.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum CategoryStatusEnum implements IEnum<Integer> {
    ENABLED(1, "启用"),
    DISABLED(2, "禁用");

    @EnumValue
    @JsonValue
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

    @Override
    public Integer getValue() {
        return code;
    }
}
