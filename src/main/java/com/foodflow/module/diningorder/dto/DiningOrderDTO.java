package com.foodflow.module.diningorder.dto;


import com.foodflow.common.enums.OrderStatusEnum;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;


@Data
public class DiningOrderDTO {
    private Long tableId;
    private Long orderId;

    // min、max不拦截空值，可用作选择条件
    @Min(value = 1, message = "状态不能小于1")
    @Max(value = 5, message = "状态不能大于5")
    private Integer status;

    public OrderStatusEnum getStatusEnum() {
        return status == null ? null : OrderStatusEnum.ofCode(status);
    }
}
