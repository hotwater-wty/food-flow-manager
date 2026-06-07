package com.foodflow.module.diningorder.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiningOrderUpdateVO {
    private Long orderId;
    private String orderNo;
    private Integer status;
}
