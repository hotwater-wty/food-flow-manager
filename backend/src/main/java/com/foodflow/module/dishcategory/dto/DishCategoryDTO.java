package com.foodflow.module.dishcategory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "菜品分类请求参数")
public class DishCategoryDTO {

    @Schema(description = "分类名称，最长50个字符", example = "热销菜品", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    private String name;

    @Schema(description = "排序值，数值越小越靠前", example = "0")
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort = 0;
}
