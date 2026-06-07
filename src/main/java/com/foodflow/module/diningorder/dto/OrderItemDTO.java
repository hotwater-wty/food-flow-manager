package com.foodflow.module.diningorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "订单菜品明细请求参数")
public class OrderItemDTO {

    @Schema(description = "菜品ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜品ID不能为空")
    private Long dishId;

    // Min不能拦截空
    @Schema(description = "下单数量", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;

    @Schema(description = "单品备注", example = "少辣")
    private String remark;
}
