package com.foodflow.module.orderitem.vo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemVO {
    private Long orderItemId;
    private Long orderId;
    private Long dishId;
    private String dishName;
    private String dishImage;
    private Integer dishPrice;
    private Integer quantity;
    private Integer amount;
    private String remark;
}
