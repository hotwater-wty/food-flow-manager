package com.foodflow.module.reservation.vo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationVO {
    private Long reservationId;

    private String reservationNo;

    private Long tableId;

    private String tableNo;

    private Integer peopleCount;

    private LocalDateTime reserveTime;

    private Integer status;
}
