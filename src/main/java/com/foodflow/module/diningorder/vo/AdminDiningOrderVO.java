package com.foodflow.module.diningorder.vo;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "管理端堂食订单列表响应数据")
public class AdminDiningOrderVO {

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "订单编号", example = "O202606080001")
    private String orderNo;

    @Schema(description = "堂食会话ID", example = "1")
    private Long sessionId;

    @Schema(description = "桌位ID", example = "1")
    private Long tableId;

    @Schema(description = "桌号", example = "A01")
    private String tableNo; // 冗余字段

    @Schema(description = "订单总金额，单位：分", example = "7600")
    private Integer totalAmount;

    @Schema(description = "订单状态：1-已下单，2-制作中，3-已上齐，4-已完成，5-已取消", example = "1")
    private Integer status;

    @Schema(description = "订单创建时间", example = "2026-06-08 18:45:00")
    private LocalDateTime createTime;

}


