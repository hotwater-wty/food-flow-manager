package com.foodflow.module.reservation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Reservation {
    private Long id;
    private String reservationNo;
    private Long userId;
    private Long tableId;
    private Integer peopleCount;
    private LocalDateTime reserveTime;
    private Integer status;
    private LocalDateTime checkInTime;
    private LocalDateTime cancelTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
