package com.foodflow.module.reservation.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationDTO {
    @NotNull(message = "桌位ID不能为空")
    private Long tableId;
    @NotNull(message = "人数不能为空")
    private Integer peopleCount;
    @NotNull(message = "预约时间不能为空")
    private LocalDateTime reserveTime;
}
