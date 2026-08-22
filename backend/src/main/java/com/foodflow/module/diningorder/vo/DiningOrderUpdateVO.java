package com.foodflow.module.diningorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "堂食订单状态修改响应数据")
public class DiningOrderUpdateVO {
    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "订单编号", example = "O202606080001")
    private String orderNo;

    @Schema(description = "更新后的订单状态：1-已下单，2-制作中，3-已上齐，4-已完成，5-已取消", example = "2")
    private Integer status;
}
