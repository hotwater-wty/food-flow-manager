package com.foodflow.module.dish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "菜品基础数据")
public class DishDTO {
    @Schema(description = "菜品ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜品id不能为空")
    private Long id;

    @Schema(description = "菜品分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "菜品名称", example = "宫保鸡丁")
    private String name;

    @Schema(description = "菜品价格，单位：分", example = "3800")
    private Integer price;
}
