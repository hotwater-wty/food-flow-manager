package com.foodflow.module.diningorder.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.foodflow.module.orderitem.vo.OrderItemVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDiningOrderDetailVO {

    private Long orderId;
    private String orderNo;
    private Long sessionId;
    private Long tableId;
    private String tableNo; // 冗余字段
    private Integer totalAmount;
    private Integer status;
    private LocalDateTime createTime;

    private List<OrderItemVO> items;
}

