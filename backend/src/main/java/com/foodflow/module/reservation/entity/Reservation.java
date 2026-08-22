package com.foodflow.module.reservation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodflow.common.enums.ReservationStatusEnum;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("reservation")
public class Reservation {
    private Long id;
    private String reservationNo;
    private Long userId;
    private Long tableId;
    private Integer peopleCount;
    private LocalDateTime reserveTime;  // 预约时间
    private ReservationStatusEnum status;
    private LocalDateTime checkInTime;  // 到店时间
    private LocalDateTime cancelTime;  // 取消时间
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;
}
