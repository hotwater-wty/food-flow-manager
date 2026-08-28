package com.foodflow.module.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "今日热销菜品条目")
public class TopDishVO {

    @Schema(description = "菜品ID", example = "1")
    private Long dishId;

    @Schema(description = "菜品名称（下单时的快照）", example = "自动验收菜")
    private String dishName;

    @Schema(description = "今日累计销量", example = "5")
    private Long quantity;

    @Schema(description = "今日累计销售额（分）", example = "6170")
    private Long amount;
}
