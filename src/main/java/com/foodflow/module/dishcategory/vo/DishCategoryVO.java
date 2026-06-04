package com.foodflow.module.dishcategory.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DishCategoryVO {

    private Long id;

    private String name;

    private Integer sort;

    private Integer status;
}
