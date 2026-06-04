package com.foodflow.module.dish.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class DishDTO {
    @NotNull(message = "菜品id不能为空")
    private Long id;
    private Long categoryId;
    private String name;
    private Integer price;
}
