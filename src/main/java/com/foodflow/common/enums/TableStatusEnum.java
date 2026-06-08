package com.foodflow.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum TableStatusEnum implements IEnum<Integer> {
    FREE(0, "空闲"),
    RESERVED(1, "已预约"),
    WAITING(2, "等待中"),
    DINING(3, "用餐中"),
    DISABLED(4, "禁用");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String description;

    TableStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TableStatusEnum ofCode(Integer code) {
        for (TableStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown table status code: " + code);
    }

    @Override
    public Integer getValue() {
        return code;
    }
}
