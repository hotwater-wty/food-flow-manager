package com.foodflow.common.enums;

import lombok.Getter;

@Getter
public enum DishStatusEnum {
    STOPPED(0, "停售"),
    ON_SALE(1, "启售"),
    SOLD_OUT(2, "售罄");

    private final Integer code;
    private final String description;

    DishStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static DishStatusEnum ofCode(Integer code) {
        for (DishStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown dish status code: " + code);
    }
}
