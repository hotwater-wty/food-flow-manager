package com.foodflow.module.diningorder.vo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDiningOrderVO {

    private Long orderId;
    private String orderNo;
    private Long tableId;
    private String tableNo; // 冗余字段
    private Integer totalAmount;
    private Integer status;
    private LocalDateTime createTime;

}
