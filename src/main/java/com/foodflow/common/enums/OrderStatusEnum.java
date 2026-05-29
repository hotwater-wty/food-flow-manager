package com.foodflow.common.enums;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {
    PLACED(1, "已下单"),
    COOKING(2, "制作中"),
    SERVED(3, "已上齐"),
    COMPLETED(4, "已完成"),
    CANCELED(5, "已取消");

    private final Integer code;
    private final String description;

    OrderStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderStatusEnum ofCode(Integer code) {
        for (OrderStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown order status code: " + code);
    }
}
