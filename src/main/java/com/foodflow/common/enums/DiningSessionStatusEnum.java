package com.foodflow.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum DiningSessionStatusEnum implements IEnum<Integer> {
    WAITING(0, "等待中"),
    DINING(1, "用餐中"),
    COMPLETED(2, "已完成"),
    CANCELED(3, "已取消");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String description;

    DiningSessionStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static DiningSessionStatusEnum ofCode(Integer code) {
        for (DiningSessionStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown dining session status code: " + code);
    }

    @Override
    public Integer getValue() {
        return code;
    }
}
