package com.foodflow.module.reservation.vo;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "创建预约响应数据")
public class ReservationCreateVO {
    @Schema(description = "预约ID", example = "1")
    private Long reservationId;

    @Schema(description = "预约编号", example = "R202606080001")
    private String reservationNo;

    @Schema(description = "桌位ID", example = "1")
    private Long tableId;

    @Schema(description = "桌号", example = "A01")
    private String tableNo;

    @Schema(description = "预约人数", example = "4")
    private Integer peopleCount;

    @Schema(description = "预约时间", example = "2026-06-08 18:30:00")
    private LocalDateTime reserveTime;

    @Schema(description = "预约状态：0-待到店，1-已到店，2-已取消，3-已超时", example = "0")
    private Integer status;
}
