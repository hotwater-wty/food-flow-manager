package com.foodflow.module.orderitem.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemCreateVO {
    
    private Long dishId;
    private String dishName;
    private Integer dishPrice;
    private Integer quantity;
    private Integer amount;
}
