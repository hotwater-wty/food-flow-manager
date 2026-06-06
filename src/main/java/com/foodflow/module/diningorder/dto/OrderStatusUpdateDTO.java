package com.foodflow.module.diningorder.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class OrderStatusUpdateDTO {
    
    @NotNull(message = "订单状态不能为空")
    @Min(value = 1, message = "订单状态必须大于等于1")
    @Max(value = 5, message = "订单状态必须小于等于5")
    private Integer status;

}
