package com.foodflow.module.diningorder.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Schema(description = "创建堂食订单请求参数")
public class OrderItemCreateDTO {
    @Schema(description = "订单菜品明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private List<@Valid OrderItemDTO> items;
}
