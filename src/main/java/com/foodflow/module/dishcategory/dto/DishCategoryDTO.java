package com.foodflow.module.dishcategory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DishCategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    private String name;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort = 0;
}
