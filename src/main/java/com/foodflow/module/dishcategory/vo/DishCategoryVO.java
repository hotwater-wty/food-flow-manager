package com.foodflow.module.dishcategory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "菜品分类响应数据")
public class DishCategoryVO {

    @Schema(description = "菜品分类ID", example = "1")
    private Long id;

    @Schema(description = "分类名称", example = "热销菜品")
    private String name;

    @Schema(description = "排序值，数值越小越靠前", example = "0")
    private Integer sort;

    @Schema(description = "分类状态：1-启用，2-禁用", example = "1")
    private Integer status;
}
