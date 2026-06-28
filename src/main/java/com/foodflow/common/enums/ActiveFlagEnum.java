package com.foodflow.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum ActiveFlagEnum implements IEnum<Integer> {
    // INACTIVE(null, "未激活"),    // 会报空指针异常，业务中直接使用null表示未激活
    ACTIVE(1, "已激活");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String description;

    ActiveFlagEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ActiveFlagEnum ofCode(Integer code) { 
        for (ActiveFlagEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown active flag code: " + code);
    }

    @Override
    public Integer getValue() {
        return code;
    }
}
