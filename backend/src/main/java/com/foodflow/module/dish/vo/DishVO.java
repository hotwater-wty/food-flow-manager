package com.foodflow.module.dish.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "菜品响应数据")
public class DishVO {

    @Schema(description = "菜品ID", example = "1")
    private Long id;

    @Schema(description = "菜品分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "菜品名称", example = "宫保鸡丁")
    private String name;

    @Schema(description = "菜品价格，单位：分", example = "3800")
    private Integer price;

    @Schema(description = "菜品图片地址", example = "https://example.com/dish/gongbao.jpg")
    private String image;

    @Schema(description = "菜品描述", example = "经典川味热菜，微辣")
    private String description;

    @Schema(description = "菜品状态：0-停售，1-启售，2-售罄", example = "1")
    private Integer status;
}
