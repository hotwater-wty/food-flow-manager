package com.foodflow.module.diningorder.dto;


import com.foodflow.common.enums.OrderStatusEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;


@Data
@Schema(description = "堂食订单查询条件")
public class DiningOrderDTO {
    @Schema(description = "桌位ID", example = "1")
    private Long tableId;

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    // min、max不拦截空值，可用作选择条件
    @Schema(description = "订单状态：1-已下单，2-制作中，3-已上齐，4-已完成，5-已取消", example = "1")
    @Min(value = 1, message = "状态不能小于1")
    @Max(value = 5, message = "状态不能大于5")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    public OrderStatusEnum getStatusEnum() {
        return status == null ? null : OrderStatusEnum.ofCode(status);
    }
}
