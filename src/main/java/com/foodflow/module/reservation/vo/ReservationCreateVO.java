package com.foodflow.module.reservation.vo;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationCreateVO {
    private Long reservationId;
    private String reservationNo;
    private Long tableId;
    private String tableNo;
    private Integer peopleCount;
    private LocalDateTime reserveTime;
    private Integer status;
}
