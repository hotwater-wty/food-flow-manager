package com.foodflow.module.diningorder.vo;

import java.util.List;

import com.foodflow.common.enums.OrderStatusEnum;
import com.foodflow.module.orderitem.vo.OrderItemCreateVO;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiningOrderCreateVO {

    private Long orderId;
    private String orderNo;
    private Long sessionId;
    private Long tableId;
    private String tableNo;
    private Integer totalAmount;
    private OrderStatusEnum status;

    private List<OrderItemCreateVO> items;
}
