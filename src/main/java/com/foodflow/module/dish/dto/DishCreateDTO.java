package com.foodflow.module.dish.dto;

import lombok.Data;

import com.foodflow.common.enums.DishStatusEnum;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class DishCreateDTO {
    @NotNull(message = "菜品分类id不能为空")
    private Long categoryId;

    @NotNull(message = "菜品名称不能为空")
    @Size(min = 1, max = 20, message = "菜品名称不能小于1个字符或大于20个字符")
    private String name;

    @NotNull(message = "菜品描述不能为空")
    @Size(min = 1, max = 200, message = "菜品描述不能小于1个字符或大于200个字符")
    private String description;

    @NotNull(message = "菜品价格不能为空")
    @Min(value = 0, message = "菜品价格不能小于0")
    private Integer price;

    @NotBlank(message = "菜品图片不能为空")
    private String image;

    @Min(value = 0, message = "菜品状态错误")
    @Max(value = 1, message = "菜品状态错误")
    private Integer status = DishStatusEnum.STOPPED.getCode();
}
