package com.foodflow.module.orderitem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "创建订单菜品明细响应数据")
public class OrderItemCreateVO {

    @Schema(description = "菜品ID", example = "1")
    private Long dishId;

    @Schema(description = "菜品名称", example = "宫保鸡丁")
    private String dishName;

    @Schema(description = "菜品单价，单位：分", example = "3800")
    private Integer dishPrice;

    @Schema(description = "下单数量", example = "2")
    private Integer quantity;

    @Schema(description = "明细金额，单位：分", example = "7600")
    private Integer amount;
}
