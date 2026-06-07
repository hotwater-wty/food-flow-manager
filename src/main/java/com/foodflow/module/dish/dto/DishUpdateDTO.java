package com.foodflow.module.dish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "修改菜品请求参数")
public class DishUpdateDTO {
    @Schema(description = "菜品分类ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜品分类id不能为空")
    private Long categoryId;

    @Schema(description = "菜品名称，1到20个字符", example = "宫保鸡丁", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜品名称不能为空")
    @Size(min = 1, max = 20, message = "菜品名称不能小于1个字符或大于20个字符")
    private String name;

    @Schema(description = "菜品描述，1到200个字符", example = "经典川味热菜，微辣", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜品描述不能为空")
    @Size(min = 1, max = 200, message = "菜品描述不能小于1个字符或大于200个字符")
    private String description;

    @Schema(description = "菜品价格，单位：分", example = "3800", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜品价格不能为空")
    @Min(value = 0, message = "菜品价格不能小于0")
    private Integer price;

    @Schema(description = "菜品图片地址", example = "https://example.com/dish/gongbao.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜品图片不能为空")
    private String image;
}
