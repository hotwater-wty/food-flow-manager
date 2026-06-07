package com.foodflow.module.reservation.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationDTO {
    @NotNull(message = "桌位ID不能为空")
    private Long tableId;

    @NotNull(message = "人数不能为空")
    @Min(value = 1, message = "人数必须大于等于1人")
    private Integer peopleCount;

    @NotNull(message = "预约时间不能为空")
    @Future(message = "预约时间必须在当前时间之后")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:SS")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:SS")
    private LocalDateTime reserveTime;
}
