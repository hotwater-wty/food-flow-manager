package com.foodflow.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum ReservationStatusEnum implements IEnum<Integer> {
    WAITING_CHECK_IN(0, "待到店"),
    CHECKED_IN(1, "已到店"),
    CANCELED(2, "已取消"),
    TIMEOUT(3, "已超时");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String description;

    ReservationStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ReservationStatusEnum ofCode(Integer code) {
        for (ReservationStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown reservation status code: " + code);
    }

    @Override
    public Integer getValue() {
        return code;
    }
}
