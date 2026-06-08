package com.foodflow.module.diningorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
@Schema(description = "堂食订单状态修改请求参数")
public class OrderStatusUpdateDTO {

    @Schema(description = "订单状态：1-已下单，2-制作中，3-已上齐，4-已完成，5-已取消", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单状态不能为空")
    @Min(value = 1, message = "订单状态必须大于等于1")
    @Max(value = 5, message = "订单状态必须小于等于5")
    private Integer status;

}
