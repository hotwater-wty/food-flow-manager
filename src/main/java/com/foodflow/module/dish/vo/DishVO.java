package com.foodflow.module.dish.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DishVO {
    
    private Long id;
    private Long categoryId;
    private String name;
    private Integer price;
    private String image;
    private String description;
    private Integer status;
}
